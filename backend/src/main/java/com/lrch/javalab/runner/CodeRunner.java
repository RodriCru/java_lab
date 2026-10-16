package com.lrch.javalab.runner;

/** Contrato del motor de ejecucion del backend. */
public interface CodeRunner {

    /** Identificador del motor, se reporta en la respuesta. */
    String nombre();

    /** Indica si el motor esta disponible (por ejemplo, si el runner-service responde). */
    boolean disponible();

    ExecutionResult ejecutar(ExecutionRequest request);
}
