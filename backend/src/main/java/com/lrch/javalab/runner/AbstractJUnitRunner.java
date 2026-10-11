package com.lrch.javalab.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Logica comun a los motores de ejecucion: prepara los archivos, lanza el proceso,
 * interpreta el resultado y limpia los temporales.
 *
 * <p>Los motores concretos solo deciden COMO se lanza el proceso (Docker o local).
 */
public abstract class AbstractJUnitRunner implements CodeRunner {

    /** El script del contenedor devuelve 20 cuando la compilacion falla. */
    protected static final int EXIT_COMPILE_ERROR = 20;
    protected static final String TEST_CLASS = "SolutionTest";

    private static final int MAX_CONSOLE_CHARS = 4_000;

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final RunnerProperties properties;

    private final JUnitReportParser parser = new JUnitReportParser();

    protected AbstractJUnitRunner(RunnerProperties properties) {
        this.properties = properties;
    }

    @Override
    public ExecutionResult ejecutar(ExecutionRequest request) {
        Path trabajo = null;
        try {
            trabajo = Files.createTempDirectory("javalab-");
            Path srcDir = Files.createDirectories(trabajo.resolve("src"));
            Path outDir = Files.createDirectories(trabajo.resolve("out"));

            Files.writeString(srcDir.resolve(request.className() + ".java"), request.studentCode(),
                    StandardCharsets.UTF_8);
            Files.writeString(srcDir.resolve(TEST_CLASS + ".java"), CapturaDeSalida.conExtension(request.testCode()),
                    StandardCharsets.UTF_8);
            Files.writeString(srcDir.resolve(CapturaDeSalida.CLASE + ".java"), CapturaDeSalida.FUENTE,
                    StandardCharsets.UTF_8);

            long inicio = System.currentTimeMillis();
            SalidaProceso bruta = ejecutarProceso(srcDir, outDir);
            CapturaDeSalida.Extraido capturado = CapturaDeSalida.extraer(bruta.texto());
            SalidaProceso salida = new SalidaProceso(bruta.codigo(), capturado.consola(), bruta.timeout());
            long duracion = System.currentTimeMillis() - inicio;

            if (salida.timeout()) {
                return new ExecutionResult(ExecutionResult.Status.TIMEOUT, false, List.of(), null,
                        recortar(salida.texto()), duracion, nombre());
            }
            if (salida.codigo() == EXIT_COMPILE_ERROR) {
                return new ExecutionResult(ExecutionResult.Status.COMPILE_ERROR, false, List.of(),
                        recortar(salida.texto()), null, duracion, nombre());
            }

            List<TestCaseOutcome> tests = parser.parse(outDir.resolve("reports")).stream()
                    .map(t -> t.conEntrada(TestSourceInspector.cuerpoDe(request.testCode(), t.name()))
                            .conSalida(capturado.salidas().get(t.name())))
                    .toList();
            if (tests.isEmpty()) {
                log.warn("JUnit no genero reporte (exit={}, motor={})", salida.codigo(), nombre());
                return new ExecutionResult(ExecutionResult.Status.INTERNAL_ERROR, true, List.of(), null,
                        recortar(salida.texto()), duracion, nombre());
            }

            boolean todosPasan = tests.stream().allMatch(TestCaseOutcome::passed);
            return new ExecutionResult(
                    todosPasan ? ExecutionResult.Status.PASSED : ExecutionResult.Status.FAILED,
                    true, tests, null, recortar(salida.texto()), duracion, nombre());

        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo preparar la ejecucion del ejercicio", ex);
        } finally {
            borrarDirectorio(trabajo);
        }
    }

    /** Lanza el proceso que compila y ejecuta los tests. */
    protected abstract SalidaProceso ejecutarProceso(Path srcDir, Path outDir) throws IOException;

    protected record SalidaProceso(int codigo, String texto, boolean timeout) {
    }

    /** Permite a cada motor ajustar las variables de entorno del proceso hijo (por defecto hereda todas). */
    protected void prepararEntorno(Map<String, String> entorno) {
    }

    /**
     * Ejecuta un comando, captura su salida y lo mata si excede el timeout configurado.
     *
     * @param cancelar accion extra al agotarse el tiempo (por ejemplo, matar el contenedor)
     */
    protected SalidaProceso lanzar(List<String> comando, Path directorio, Runnable cancelar) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(comando)
                .directory(directorio.toFile())
                .redirectErrorStream(true);

        prepararEntorno(builder.environment());
        Process proceso = builder.start();
        proceso.getOutputStream().close();

        StringBuilder buffer = new StringBuilder();
        Thread lector = new Thread(() -> leerSalida(proceso, buffer), "javalab-salida");
        lector.setDaemon(true);
        lector.start();

        boolean termino;
        try {
            termino = proceso.waitFor(properties.timeoutSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            termino = false;
        }

        if (!termino) {
            log.info("Ejecucion cancelada por exceder {} segundos", properties.timeoutSeconds());
            cancelar(proceso, cancelar);
            return new SalidaProceso(-1, texto(buffer), true);
        }

        unir(lector);
        return new SalidaProceso(proceso.exitValue(), texto(buffer), false);
    }

    private void cancelar(Process proceso, Runnable cancelar) {
        if (cancelar != null) {
            try {
                cancelar.run();
            } catch (RuntimeException ex) {
                log.debug("No se pudo cancelar el proceso: {}", ex.getMessage());
            }
        }
        proceso.destroyForcibly();
        try {
            proceso.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void leerSalida(Process proceso, StringBuilder buffer) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                synchronized (buffer) {
                    buffer.append(linea).append('\n');
                }
            }
        } catch (IOException ex) {
            log.debug("Lectura de salida interrumpida: {}", ex.getMessage());
        }
    }

    private String texto(StringBuilder buffer) {
        synchronized (buffer) {
            return buffer.toString();
        }
    }

    private void unir(Thread hilo) {
        try {
            hilo.join(2_000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    protected String recortar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.strip();
        if (limpio.isEmpty()) {
            return null;
        }
        return limpio.length() <= MAX_CONSOLE_CHARS
                ? limpio
                : limpio.substring(0, MAX_CONSOLE_CHARS) + "\n... (salida recortada)";
    }

    private void borrarDirectorio(Path dir) {
        if (dir == null) {
            return;
        }
        try (Stream<Path> rutas = Files.walk(dir)) {
            rutas.sorted(Comparator.reverseOrder()).forEach(ruta -> {
                try {
                    Files.deleteIfExists(ruta);
                } catch (IOException ex) {
                    log.debug("No se pudo borrar {}: {}", ruta, ex.getMessage());
                }
            });
        } catch (IOException ex) {
            log.debug("No se pudo limpiar {}: {}", dir, ex.getMessage());
        }
    }
}
