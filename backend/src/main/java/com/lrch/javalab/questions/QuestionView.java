package com.lrch.javalab.questions;

import java.util.List;

/** Lo que ve el alumno: la pregunta sin la respuesta correcta, mas su estado personal. */
public record QuestionView(
        String id,
        String topic,
        String difficulty,
        String question,
        String code,
        List<String> options,
        String status) {
}
