package com.lrch.javalab.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Un ejercicio del catalogo. Se carga desde un archivo YAML en el directorio de ejercicios.
 *
 * <p>El alumno escribe la clase completa ({@link #className}) y el metodo declarado en
 * {@link #methodSignature()}. Los tests ({@link #tests()}) y la solucion de referencia
 * ({@link #referenceSolution()}) nunca se envian al cliente.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Exercise(
        String slug,
        int level,
        String topic,
        String title,
        String statement,
        String methodSignature,
        String className,
        Integer estimatedMinutes,
        String difficulty,
        List<String> hints,
        String starterCode,
        String referenceSolution,
        String tests,
        ComplexityQuestion complexityQuestion) {

    public boolean hasComplexityQuestion() {
        return complexityQuestion != null;
    }

    public List<String> safeHints() {
        return hints == null ? List.of() : hints;
    }
}
