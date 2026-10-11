package com.lrch.javalab.catalog;

import java.util.List;

/** Lo que ve el alumno de la pregunta de complejidad: sin la respuesta correcta. */
public record ComplexityQuestionView(String prompt, List<String> options) {
}
