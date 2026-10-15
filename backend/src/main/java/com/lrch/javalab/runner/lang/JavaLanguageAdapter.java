package com.lrch.javalab.runner.lang;

import com.lrch.javalab.catalog.Exercise;
import com.lrch.javalab.runner.CapturaDeSalida;
import com.lrch.javalab.runner.SourceValidator;
import org.springframework.stereotype.Component;

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
@Component
public class JavaLanguageAdapter implements LanguageAdapter {

    /** Clase de tests esperada por el motor de Java. */
    public static final String TEST_CLASS = "SolutionTest";

    private final SourceValidator validator;

    public JavaLanguageAdapter(SourceValidator validator) {
        this.validator = validator;
    }

    @Override
    public String language() {
        return "java";
    }

    @Override
    public String image() {
        return "javalab-runner:17";
    }

    @Override
    public void validar(String codigo, Exercise ejercicio) {
        validator.validar(codigo, ejercicio.className());
    }

    @Override
    public int escribirEntrada(Path srcDir, Exercise ejercicio, String codigoAlumno) {
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
