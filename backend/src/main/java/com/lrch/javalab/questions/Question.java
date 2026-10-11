package com.lrch.javalab.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Pregunta de opcion multiple del banco. {@link #correctIndex()} y {@link #explanation()} viven
 * solo en el servidor: al cliente se le entrega {@link QuestionView}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Question(
        String id,
        String topic,
        String difficulty,
        String question,
        String code,
        List<String> options,
        int correctIndex,
        String explanation) {

    public QuestionView view(String status) {
        return new QuestionView(id, topic, difficulty, question, code, options, status);
    }

    /** Devuelve los problemas de la pregunta (lista vacia si es valida). */
    public List<String> problemas() {
        java.util.ArrayList<String> errores = new java.util.ArrayList<>();
        if (id == null || id.isBlank()) {
            errores.add("falta 'id'");
        }
        if (topic == null || topic.isBlank()) {
            errores.add("falta 'topic'");
        }
        if (question == null || question.isBlank()) {
            errores.add("falta 'question'");
        }
        if (options == null || options.size() < 2) {
            errores.add("necesita al menos 2 opciones");
        } else if (correctIndex < 0 || correctIndex >= options.size()) {
            errores.add("'correctIndex' fuera de rango");
        }
        if (explanation == null || explanation.isBlank()) {
            errores.add("falta 'explanation'");
        }
        return errores;
    }
}
