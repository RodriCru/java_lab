package com.lrch.javalab.runner;

import java.util.List;

/**
 * Resultado de ejecutar el codigo de un alumno.
 *
 * @param status         desenlace global de la ejecucion
 * @param compiled       true si el codigo llego a compilar
 * @param tests          detalle de cada test ejecutado
 * @param compileOutput  salida del compilador cuando fallo la compilacion
 * @param consoleOutput  salida estandar del programa del alumno
 * @param durationMillis tiempo total de la ejecucion
 * @param backend        motor usado: "docker" o "local"
 */
public record ExecutionResult(
        Status status,
        boolean compiled,
        List<TestCaseOutcome> tests,
        String compileOutput,
        String consoleOutput,
        long durationMillis,
        String backend) {

    public enum Status {
        PASSED,
        FAILED,
        COMPILE_ERROR,
        TIMEOUT,
        INTERNAL_ERROR
    }

    public long passedCount() {
        return tests.stream().filter(TestCaseOutcome::passed).count();
    }

    public long failedCount() {
        return tests.stream().filter(t -> !t.passed() && !t.skipped()).count();
    }

    public boolean allPassed() {
        return status == Status.PASSED;
    }
}
