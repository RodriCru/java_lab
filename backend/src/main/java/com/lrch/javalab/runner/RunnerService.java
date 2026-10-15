package com.lrch.javalab.runner;

import com.lrch.javalab.common.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Elige el motor de ejecucion y expone un unico punto de entrada.
 *
 * <p>Preferencia: Docker. Si no esta disponible y el respaldo tambien falla, se reporta el
 * error en vez de ejecutar el codigo de forma insegura.
 */
@Service
public class RunnerService {

    private static final Logger log = LoggerFactory.getLogger(RunnerService.class);

    private final DockerCodeRunner dockerRunner;
    private final LocalCodeRunner localRunner;
    private final RunnerProperties properties;

    /**
     * Cola de envios: cada compilacion y ejecucion de Java consume ~150-200 MB, y el plan gratis de Render
     * tiene 512 MB. Con varios envios a la vez el contenedor se quedaria sin memoria y se reiniciaria.
     */
    private final Semaphore turnos;

    /** Un motor elegido por lenguaje (Docker suele ganar; el local solo sirve para Java). */
    private final java.util.concurrent.ConcurrentHashMap<String, CodeRunner> motores =
            new java.util.concurrent.ConcurrentHashMap<>();

    public RunnerService(DockerCodeRunner dockerRunner, LocalCodeRunner localRunner, RunnerProperties properties) {
        this.dockerRunner = dockerRunner;
        this.localRunner = localRunner;
        this.properties = properties;
        this.turnos = new Semaphore(properties.maxConcurrent(), true);
    }

    public ExecutionResult ejecutar(ExecutionRequest request) {
        esperarTurno();
        try {
            return seleccionar(request).ejecutar(request);
        } finally {
            turnos.release();
        }
    }

    private void esperarTurno() {
        boolean obtenido;
        try {
            obtenido = turnos.tryAcquire(properties.queueWaitSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            obtenido = false;
        }
        if (!obtenido) {
            throw ApiException.tooManyRequests("SERVIDOR_OCUPADO",
                    "Hay otros envíos en curso. Inténtalo de nuevo en unos segundos.");
        }
    }

    /** Motor efectivamente en uso, para mostrarlo en la respuesta de la API. */
    public String motorActivo() {
        return seleccionar(null).nombre();
    }

    /**
     * Elige el motor segun el lenguaje del ejercicio: Docker puede con cualquiera (usa la imagen del
     * adaptador), el motor local solo con Java (es {@code javac}). El resultado se cachea por lenguaje.
     */
    private CodeRunner seleccionar(ExecutionRequest request) {
        String lenguaje = request == null ? "java" : request.adapter().language();
        CodeRunner elegido = motores.get(lenguaje);
        if (elegido != null) {
            return elegido;
        }
        synchronized (this) {
            return motores.computeIfAbsent(lenguaje, this::decidir);
        }
    }

    private CodeRunner decidir(String lenguaje) {
        boolean esJava = "java".equals(lenguaje);
        if (properties.isUseDocker() && dockerRunner.disponible()) {
            log.info("Motor de ejecucion: Docker para '{}'", lenguaje);
            return dockerRunner;
        }
        if (!esJava) {
            throw ApiException.badRequest("MOTOR_NO_DISPONIBLE",
                    "El lenguaje «" + lenguaje + "» necesita Docker. Arranca Docker y ejecuta "
                            + "'runner/build.sh' para poder enviar soluciones.");
        }
        if (properties.isUseDocker()) {
            log.warn("Docker no disponible: se usara el motor local. El aislamiento es menor.");
        }
        if (localRunner.disponible()) {
            log.info("Motor de ejecucion: local (javac del equipo)");
            return localRunner;
        }
        throw new IllegalStateException(
                "No hay ningun motor de ejecucion disponible. Instala Docker y ejecuta 'runner/build.sh', "
                        + "o verifica el JDK y la ruta del jar de JUnit.");
    }

    /** Utilidad para las pruebas: listado de motores registrados. */
    public List<String> motoresDisponibles() {
        return List.of(dockerRunner, localRunner).stream()
                .filter(CodeRunner::disponible)
                .map(CodeRunner::nombre)
                .toList();
    }
}
