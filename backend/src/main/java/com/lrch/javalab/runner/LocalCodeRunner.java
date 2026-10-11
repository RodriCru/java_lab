package com.lrch.javalab.runner;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected SalidaProceso ejecutarProceso(Path srcDir, Path outDir) throws IOException {
        Path classesDir = Files.createDirectories(outDir.resolve("classes"));
        Path reportsDir = Files.createDirectories(outDir.resolve("reports"));
        String junitJar = properties.junitJarPath().toString();

        List<String> fuentes;
        try (Stream<Path> archivos = Files.list(srcDir)) {
            fuentes = archivos.filter(p -> p.toString().endsWith(".java")).map(Path::toString).sorted().toList();
        }

        List<String> compilar = new ArrayList<>(List.of(
                javacBin().toString(), "-encoding", "UTF-8", "-cp", junitJar, "-d", classesDir.toString()));
        compilar.addAll(fuentes);

        SalidaProceso compilacion = lanzar(compilar, outDir, null);
        if (compilacion.timeout()) {
            return compilacion;
        }
        if (compilacion.codigo() != 0) {
            return new SalidaProceso(EXIT_COMPILE_ERROR, compilacion.texto(), false);
        }

        List<String> ejecutar = List.of(
                javaBin().toString(), "-Dfile.encoding=UTF-8", "-jar", junitJar, "execute",
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
