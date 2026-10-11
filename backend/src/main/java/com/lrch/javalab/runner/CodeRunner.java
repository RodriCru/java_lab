package com.lrch.javalab.runner;

/** Contrato del motor de ejecucion. */
public interface CodeRunner {

    /** Identificador del motor, se reporta en la respuesta. */
    String nombre();

    /** Indica si el motor puede usarse en este equipo. */
    boolean disponible();

    ExecutionResult ejecutar(ExecutionRequest request);
}
