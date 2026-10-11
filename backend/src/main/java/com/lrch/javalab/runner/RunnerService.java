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

    private volatile CodeRunner motor;

    public RunnerService(DockerCodeRunner dockerRunner, LocalCodeRunner localRunner, RunnerProperties properties) {
        this.dockerRunner = dockerRunner;
        this.localRunner = localRunner;
        this.properties = properties;
        this.turnos = new Semaphore(properties.maxConcurrent(), true);
    }

    public ExecutionResult ejecutar(ExecutionRequest request) {
        esperarTurno();
        try {
            return seleccionar().ejecutar(request);
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
        return seleccionar().nombre();
    }

    private CodeRunner seleccionar() {
        CodeRunner elegido = motor;
        if (elegido != null) {
            return elegido;
        }
        synchronized (this) {
            if (motor == null) {
                motor = decidir();
            }
            return motor;
        }
    }

    private CodeRunner decidir() {
        if (properties.isUseDocker() && dockerRunner.disponible()) {
            log.info("Motor de ejecucion: Docker ({})", properties.image());
            return dockerRunner;
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
