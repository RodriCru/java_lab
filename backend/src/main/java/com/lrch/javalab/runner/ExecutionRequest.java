package com.lrch.javalab.runner;

/** Lo que se va a compilar y ejecutar: la clase del alumno mas los tests del ejercicio. */
public record ExecutionRequest(String className, String studentCode, String testCode) {
}
