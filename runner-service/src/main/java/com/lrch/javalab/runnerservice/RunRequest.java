package com.lrch.javalab.runnerservice;

import com.lrch.javalab.runner.ExecutionResult;
import com.lrch.javalab.runner.TestCaseOutcome;
import com.lrch.javalab.runner.lang.LanguageAdapter;
import com.lrch.javalab.runner.lang.RunnerExercise;
import com.lrch.javalab.runner.lang.RunnerException;

/**
 * Peticion de ejecucion: es lo que el backend envia al runner-service para un envio.
 *
 * @param language     lenguaje del ejercicio (java, python...)
 * @param className    clase esperada (solo Java; en otros lenguajes puede ser null)
 * @param tests        codigo de los tests del ejercicio (no sale del servidor)
 * @param sourceCode   codigo del alumno
 */
public record RunRequest(String language, String className, String tests, String sourceCode) {

    public RunnerExercise comoEjercicio() {
        return new RunnerExercise(language, className, tests);
    }

    public void validarCon(LanguageAdapter adapter) {
        if (sourceCode == null || sourceCode.isBlank()) {
            throw RunnerException.badRequest("CODIGO_VACIO", "Escribe tu solución antes de enviarla.");
        }
        adapter.validar(sourceCode, comoEjercicio());
    }
}
