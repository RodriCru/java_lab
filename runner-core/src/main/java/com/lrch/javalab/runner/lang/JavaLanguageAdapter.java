package com.lrch.javalab.runner.lang;

import com.lrch.javalab.runner.CapturaDeSalida;
import com.lrch.javalab.runner.SourceValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.UncheckedIOException;

/**
 * Adaptador de Java: mantiene el comportamiento historico del laboratorio.
 *
 * <p>El alumno escribe la clase {@code Solution}; los tests son una clase {@code SolutionTest} de
 * JUnit, y la captura por test se inyecta con {@link CapturaDeSalida}.
 */
public class JavaLanguageAdapter implements LanguageAdapter {

    /** Clase de tests esperada por el motor de Java. */
    public static final String TEST_CLASS = CapturaDeSalida.TEST_CLASS;

    private final SourceValidator validator = new SourceValidator();

    @Override
    public String language() {
        return "java";
    }

    @Override
    public void validar(String codigo, RunnerExercise ejercicio) {
        validator.validar(codigo, ejercicio.className());
    }

    @Override
    public int escribirEntrada(Path srcDir, RunnerExercise ejercicio, String codigoAlumno) {
        try {
            Files.writeString(srcDir.resolve(ejercicio.className() + ".java"), codigoAlumno,
                    StandardCharsets.UTF_8);
            Files.writeString(srcDir.resolve(TEST_CLASS + ".java"),
                    CapturaDeSalida.conExtension(ejercicio.tests()), StandardCharsets.UTF_8);
            Files.writeString(srcDir.resolve(CapturaDeSalida.CLASE + ".java"), CapturaDeSalida.FUENTE,
                    StandardCharsets.UTF_8);
            return 3;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo preparar la ejecucion del ejercicio", ex);
        }
    }
}
