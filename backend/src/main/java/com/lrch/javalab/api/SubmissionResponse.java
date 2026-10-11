package com.lrch.javalab.api;

import com.lrch.javalab.catalog.Exercise;
import com.lrch.javalab.runner.ExecutionResult;
import com.lrch.javalab.runner.TestCaseOutcome;

import java.util.List;

/** Resultado de un envio: que paso con cada test y cuanto se tardo. */
public record SubmissionResponse(
        String exerciseSlug,
        String exerciseTitle,
        String status,
        boolean solved,
        boolean compiled,
        int passed,
        int failed,
        int total,
        List<TestCaseOutcome> tests,
        String compileOutput,
        String consoleOutput,
        long durationMillis,
        String backend,
        boolean yaEstabaResuelto,
        ComplexityResult complexity) {

    public static SubmissionResponse from(Exercise ejercicio, ExecutionResult resultado, boolean resueltoFinal,
                                          boolean yaEstabaResuelto, Integer complexityAnswer) {
        return new SubmissionResponse(
                ejercicio.slug(),
                ejercicio.title(),
                resultado.status().name(),
                resueltoFinal,
                resultado.compiled(),
                (int) resultado.passedCount(),
                (int) resultado.failedCount(),
                resultado.tests().size(),
                resultado.tests(),
                resultado.compileOutput(),
                resultado.consoleOutput(),
                resultado.durationMillis(),
                resultado.backend(),
                yaEstabaResuelto,
                ComplexityResult.evaluar(ejercicio.complexityQuestion(), complexityAnswer,
                        resultado.status() == ExecutionResult.Status.PASSED));
    }
}
