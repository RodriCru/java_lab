package com.lrch.javalab.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Un ejercicio del catalogo. Se carga desde un archivo YAML en el directorio de ejercicios.
 *
 * <p>El alumno escribe la clase completa ({@link #className}) y el metodo declarado en
 * {@link #methodSignature()}. Los tests ({@link #tests()}) y la solucion de referencia
 * ({@link #referenceSolution()}) nunca se envian al cliente.
 *
 * <p>{@link #language()} indica con que lenguaje se compila y ejecuta el ejercicio
 * ({@code java}, {@code python}...). Si el YAML no lo declara, se asume {@code java}, de modo que
 * los ejercicios que ya existian siguen funcionando sin tocarlos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Exercise(
        String language,
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

    /** Identificador por defecto cuando el YAML no declara lenguaje. */
    public static final String LENGUAJE_POR_DEFECTO = "java";

    /** Lenguaje normalizado a minusculas; nunca nulo. */
    public String lenguaje() {
        return language == null || language.isBlank()
                ? LENGUAJE_POR_DEFECTO
                : language.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public boolean hasComplexityQuestion() {
        return complexityQuestion != null;
    }

    public List<String> safeHints() {
        return hints == null ? List.of() : hints;
    }
}
