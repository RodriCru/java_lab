package com.lrch.javalab.runner.lang;

import com.lrch.javalab.catalog.Exercise;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Adaptador de Python: pytest ejecuta los tests del ejercicio.
 *
 * <p>El alumno escribe su codigo en {@code solution.py} (funciones sueltas, sin clase) y los tests
 * del ejercicio van a {@code test_solution.py}. El contenedor corre {@code pytest} y genera un XML
 * con el mismo esquema que JUnit, asi que el parser del motor lo lee sin cambios.
 */
@Component
public class PythonLanguageAdapter implements LanguageAdapter {

    /** Nombre del archivo donde se escribe el codigo del alumno. */
    public static final String ARCHIVO_ALUMNO = "solution.py";
    /** Nombre del archivo de tests; pytest lo descubre por el prefijo {@code test_}. */
    public static final String ARCHIVO_TESTS = "test_solution.py";

    private final PythonSourceValidator validator;

    public PythonLanguageAdapter(PythonSourceValidator validator) {
        this.validator = validator;
    }

    @Override
    public String language() {
        return "python";
    }

    @Override
    public String image() {
        return "javalab-runner-python:3.12";
    }

    @Override
    public void validar(String codigo, Exercise ejercicio) {
        validator.validar(codigo);
    }

    @Override
    public int escribirEntrada(Path srcDir, Exercise ejercicio, String codigoAlumno) {
        try {
            Files.writeString(srcDir.resolve(ARCHIVO_ALUMNO), codigoAlumno, StandardCharsets.UTF_8);
            Files.writeString(srcDir.resolve(ARCHIVO_TESTS), ejercicio.tests(), StandardCharsets.UTF_8);
            return 2;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo preparar la ejecucion del ejercicio", ex);
        }
    }
}
