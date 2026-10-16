package com.lrch.javalab.runner;

/**
 * Resultado de un test. {@code input} es el cuerpo del test (la llamada con sus argumentos) y
 * {@code output} lo que el codigo del alumno imprimio en consola durante ese test; ambos pueden ser null.
 */
public record TestCaseOutcome(String name, boolean passed, boolean skipped, String message, long durationMillis,
                              String input, String output) {

    public TestCaseOutcome conSalida(String nuevaSalida) {
        return new TestCaseOutcome(name, passed, skipped, message, durationMillis, input, nuevaSalida);
    }

    public TestCaseOutcome conEntrada(String nuevaEntrada) {
        return new TestCaseOutcome(name, passed, skipped, message, durationMillis, nuevaEntrada, output);
    }
}
