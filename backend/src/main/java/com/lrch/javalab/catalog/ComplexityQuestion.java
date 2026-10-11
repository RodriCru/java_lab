package com.lrch.javalab.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Pregunta de complejidad de un ejercicio (opción múltiple).
 *
 * <p>{@link #correctIndex()} y {@link #explanation()} viven solo en el servidor: al cliente se le
 * entrega {@link ComplexityQuestionView}, y la explicación solo se revela tras un envío.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ComplexityQuestion(
        String prompt,
        List<String> options,
        int correctIndex,
        String explanation) {

    public ComplexityQuestionView view() {
        return new ComplexityQuestionView(prompt, options);
    }

    public boolean isValid() {
        return prompt != null && !prompt.isBlank()
                && options != null && options.size() >= 2
                && correctIndex >= 0 && correctIndex < options.size()
                && explanation != null && !explanation.isBlank();
    }
}
