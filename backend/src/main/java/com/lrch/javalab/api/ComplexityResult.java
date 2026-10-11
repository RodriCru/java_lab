package com.lrch.javalab.api;

import com.lrch.javalab.catalog.ComplexityQuestion;

/**
 * Resultado de la pregunta de complejidad. Solo se calcula cuando los tests pasan; mientras
 * tanto la respuesta correcta y la explicación no salen del servidor.
 */
public record ComplexityResult(boolean answered, boolean correct, Integer correctIndex, String explanation) {

    /** Devuelve {@code null} si el ejercicio no tiene pregunta o los tests no pasaron. */
    public static ComplexityResult evaluar(ComplexityQuestion pregunta, Integer respuesta, boolean testsPasaron) {
        if (pregunta == null || !testsPasaron) {
            return null;
        }
        if (respuesta == null) {
            return new ComplexityResult(false, false, null, null);
        }
        boolean correcta = respuesta == pregunta.correctIndex();
        return new ComplexityResult(true, correcta, pregunta.correctIndex(), pregunta.explanation());
    }
}
