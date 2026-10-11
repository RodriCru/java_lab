package com.lrch.javalab.catalog;

/**
 * Detalle de un ejercicio.
 *
 * <p>Nunca incluye la solucion de referencia ni los tests. Las pistas se entregan de a una
 * mediante {@code GET /exercises/{slug}/hints/{index}}.
 */
public record ExerciseDetail(
        String slug,
        int level,
        String topic,
        String topicName,
        String topicDescription,
        String topicTheory,
        String title,
        String statement,
        String methodSignature,
        String className,
        String starterCode,
        String difficulty,
        Integer estimatedMinutes,
        ComplexityQuestionView complexityQuestion,
        int testsCount,
        int hintsCount,
        boolean solved,
        boolean attempted,
        int attempts,
        String savedCode,
        String nextSlug,
        String nextTitle) {
}
