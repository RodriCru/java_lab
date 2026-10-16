package com.lrch.javalab.runnerservice;

import com.lrch.javalab.runner.ExecutionResult;
import com.lrch.javalab.runner.JUnitReportParser;
import com.lrch.javalab.runner.TestCaseOutcome;
import com.lrch.javalab.runner.TestSourceInspector;
import com.lrch.javalab.runner.lang.LanguageAdapter;
import com.lrch.javalab.runner.lang.LanguageRegistry;
import com.lrch.javalab.runner.lang.RunnerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Compila y ejecuta el codigo del alumno SIN Docker: el proceso corre aqui mismo, con limites de
 * tiempo y recursos. Es el corazon del runner-service.
 *
 * <p>Los archivos se preparan en un directorio temporal del propio servicio (aqui si es visible,
 * porque no hay contenedor de por medio) y se borran al terminar.
 */
@Service
public class RunnerEngine {

    private static final Logger log = LoggerFactory.getLogger(RunnerEngine.class);
    private static final int MAX_CONSOLE_CHARS = 4_000;

    private final LanguageRegistry registry;
    private final JUnitReportParser parser = new JUnitReportParser();

    public RunnerEngine() {
        this.registry = new LanguageRegistry(
                new com.lrch.javalab.runner.lang.JavaLanguageAdapter(),
                new com.lrch.javalab.runner.lang.PythonLanguageAdapter());
    }

    public ExecutionResult ejecutar(RunRequest request) {
        LanguageAdapter adapter = registry.porLenguaje(request.language())
                .orElseThrow(() -> RunnerException.badRequest("LENGUAJE_NO_SOPORTADO",
                        "El runner todavía no ejecuta «" + request.language() + "»."));
        request.validarCon(adapter);

        Path trabajo = null;
        try {
            trabajo = Files.createTempDirectory("javalab-");
            Path srcDir = Files.createDirectories(trabajo.resolve("src"));
            Path outDir = Files.createDirectories(trabajo.resolve("out"));
            adapter.escribirEntrada(srcDir, request.comoEjercicio(), request.sourceCode());

            long inicio = System.currentTimeMillis();
            Salida salida = ejecutarProceso(adapter, srcDir, outDir);
            long duracion = System.currentTimeMillis() - inicio;

            if (salida.timeout()) {
                return new ExecutionResult(ExecutionResult.Status.TIMEOUT, false, List.of(), null,
                        recortar(salida.texto()), duracion, "service");
            }
            if (salida.codigo() == 20) {
                return new ExecutionResult(ExecutionResult.Status.COMPILE_ERROR, false, List.of(),
                        recortar(salida.texto()), null, duracion, "service");
            }

            List<TestCaseOutcome> parseados = parser.parse(outDir.resolve("reports"));
            List<TestCaseOutcome> tests = new ArrayList<>(parseados.size());
            for (TestCaseOutcome t : parseados) {
                tests.add(t.conEntrada(TestSourceInspector.cuerpoDe(request.tests(), t.name())));
            }
            if (tests.isEmpty()) {
                log.warn("No se genero reporte (exit={}, lenguaje={})", salida.codigo(), adapter.language());
                return new ExecutionResult(ExecutionResult.Status.INTERNAL_ERROR, true, List.of(), null,
                        recortar(salida.texto()), duracion, "service");
            }

            boolean todosPasan = tests.stream().allMatch(TestCaseOutcome::passed);
            return new ExecutionResult(
                    todosPasan ? ExecutionResult.Status.PASSED : ExecutionResult.Status.FAILED,
                    true, tests, null, recortar(salida.texto()), duracion, "service");

        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo preparar la ejecucion", ex);
        } finally {
            borrarDirectorio(trabajo);
        }
    }

    /** Lanza el comando del lenguaje; el script/logica de cada lenguaje vive en {@code runner/<lenguaje>/}. */
    private Salida ejecutarProceso(LanguageAdapter adapter, Path srcDir, Path outDir) throws IOException {
        List<String> comando = switch (adapter.language()) {
            case "java" -> comandoJava(srcDir, outDir);
            case "python" -> comandoPython(srcDir, outDir);
            default -> throw RunnerException.badRequest("LENGUAJE_NO_SOPORTADO",
                    "Sin comando para «" + adapter.language() + "».");
        };
        return lanzar(comando, outDir);
    }

    private List<String> comandoJava(Path srcDir, Path outDir) {
        Path junit = Path.of(System.getenv().getOrDefault("JAVALAB_JUNIT_JAR",
                "/app/runner/junit-platform-console-standalone.jar"));
        Path clases = outDir.resolve("classes");
        // Se compila y, SOLO si compila, se ejecutan los tests. Si javac falla, se sale con 20
        // (COMPILE_ERROR) para no confundir "no compiló" con "un test falló".
        return List.of("sh", "-c",
                "javac -encoding UTF-8 -cp " + junit + " -d " + clases + " " + srcDir + "/*.java || exit 20; "
                        + "java -jar " + junit + " execute --class-path " + clases
                        + " --select-class SolutionTest --reports-dir=" + outDir.resolve("reports")
                        + " --details=none --disable-ansi-colors --fail-if-no-tests");
    }

    private List<String> comandoPython(Path srcDir, Path outDir) {
        return List.of("sh", "-c",
                "cd " + srcDir + " && PYTHONDONTWRITEBYTECODE=1 python3 -m pytest test_solution.py"
                        + " --junitxml=" + outDir.resolve("reports/TEST-solution.xml")
                        + " -p no:cacheprovider -q");
    }

    private record Salida(int codigo, String texto, boolean timeout) {
    }

    private Salida lanzar(List<String> comando, Path directorio) throws IOException {
        Files.createDirectories(directorio.resolve("classes"));
        Files.createDirectories(directorio.resolve("reports"));

        ProcessBuilder builder = new ProcessBuilder(comando)
                .directory(directorio.toFile())
                .redirectErrorStream(true);
        Process proceso = builder.start();
        proceso.getOutputStream().close();

        StringBuilder buffer = new StringBuilder();
        Thread lector = new Thread(() -> leerSalida(proceso, buffer), "javalab-salida");
        lector.setDaemon(true);
        lector.start();

        boolean termino;
        try {
            termino = proceso.waitFor(timeoutSegundos(), TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            termino = false;
        }
        if (!termino) {
            proceso.destroyForcibly();
            return new Salida(-1, buffer.toString(), true);
        }
        try {
            lector.join(2_000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return new Salida(proceso.exitValue(), buffer.toString(), false);
    }

    private int timeoutSegundos() {
        String valor = System.getenv("JAVALAB_RUNNER_TIMEOUT_SECONDS");
        try {
            return valor == null ? 10 : Math.max(1, Math.min(60, Integer.parseInt(valor)));
        } catch (NumberFormatException ex) {
            return 10;
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

    private String recortar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.strip();
        if (limpio.isEmpty()) {
            return null;
        }
        return limpio.length() <= MAX_CONSOLE_CHARS
                ? limpio : limpio.substring(0, MAX_CONSOLE_CHARS) + "\n... (salida recortada)";
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
