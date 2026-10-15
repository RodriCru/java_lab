package com.lrch.javalab.catalog;

/** Ejercicio tal como aparece en la lista: sin enunciado ni codigo. */
public record ExerciseSummary(
        String slug,
        String language,
        int level,
        String topic,
        String topicName,
        String title,
        String difficulty,
        Integer estimatedMinutes,
        int testsCount,
        boolean solved,
        boolean attempted) {
}
