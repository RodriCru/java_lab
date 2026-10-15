package com.lrch.javalab.runner;

import org.springframework.stereotype.Component;

import com.lrch.javalab.runner.lang.LanguageAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Motor de respaldo: compila y ejecuta con el JDK del equipo, sin Docker.
 *
 * <p>Se usa cuando Docker no esta disponible. El aislamiento es mucho menor, por eso los
 * ejercicios estan limitados por {@link SourceValidator}.
 */
@Component
public class LocalCodeRunner extends AbstractJUnitRunner {

    public LocalCodeRunner(RunnerProperties properties) {
        super(properties);
    }

    @Override
    public String nombre() {
        return "local";
    }

    @Override
    public boolean disponible() {
        boolean hayJavac = Files.isExecutable(javacBin());
        boolean hayJar = Files.isReadable(properties.junitJarPath());
        if (!hayJar) {
            log.warn("No se encuentra el jar de JUnit en {}. Ejecuta 'runner/build.sh'.",
                    properties.junitJarPath());
        }
        return hayJavac && hayJar;
    }

    /**
     * El codigo del alumno corre en el mismo contenedor que la API: no debe heredar sus variables de
     * entorno (secreto JWT, API key de Resend, credenciales de la base de datos).
     */
    @Override
    protected void prepararEntorno(Map<String, String> entorno) {
        entorno.clear();
    }

    /**
     * El motor local solo sabe compilar Java (usa el {@code javac} del equipo). Si el ejercicio es de
     * otro lenguaje, se rechaza en vez de intentar compilarlo mal.
     */
    @Override
    protected SalidaProceso ejecutarProceso(Path srcDir, Path outDir, LanguageAdapter adapter) throws IOException {
        if (!"java".equals(adapter.language())) {
            return new SalidaProceso(EXIT_COMPILE_ERROR,
                    "[javalab] El motor local solo ejecuta Java. Arranca Docker para el lenguaje '"
                            + adapter.language() + "'.", false);
        }

        Path classesDir = Files.createDirectories(outDir.resolve("classes"));
        Path reportsDir = Files.createDirectories(outDir.resolve("reports"));
        String junitJar = properties.junitJarPath().toString();

        List<String> fuentes;
        try (Stream<Path> archivos = Files.list(srcDir)) {
            fuentes = archivos.filter(p -> p.toString().endsWith(".java")).map(Path::toString).sorted().toList();
        }

        List<String> compilar = new ArrayList<>(List.of(
                javacBin().toString(), "-J-Xmx128m", "-encoding", "UTF-8", "-cp", junitJar, "-d", classesDir.toString()));
        compilar.addAll(fuentes);

        SalidaProceso compilacion = lanzar(compilar, outDir, null);
        if (compilacion.timeout()) {
            return compilacion;
        }
        if (compilacion.codigo() != 0) {
            return new SalidaProceso(EXIT_COMPILE_ERROR, compilacion.texto(), false);
        }

        List<String> ejecutar = List.of(
                javaBin().toString(), "-Xmx128m", "-Xss512k", "-Dfile.encoding=UTF-8", "-jar", junitJar, "execute",
                "--class-path", classesDir.toString(),
                "--select-class", TEST_CLASS,
                "--reports-dir", reportsDir.toString(),
                "--details=none",
                "--disable-ansi-colors",
                "--fail-if-no-tests");

        SalidaProceso ejecucion = lanzar(ejecutar, outDir, null);
        return new SalidaProceso(ejecucion.codigo(), compilacion.texto() + ejecucion.texto(), ejecucion.timeout());
    }

    private Path javacBin() {
        return Path.of(System.getProperty("java.home"), "bin", "javac");
    }

    private Path javaBin() {
        return Path.of(System.getProperty("java.home"), "bin", "java");
    }
}
