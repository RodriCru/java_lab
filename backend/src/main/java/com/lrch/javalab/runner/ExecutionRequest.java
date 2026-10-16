package com.lrch.javalab.runner;

import com.lrch.javalab.catalog.Exercise;

/**
 * Lo que se va a ejecutar: el ejercicio y el codigo del alumno. El runner-service se encarga de
 * validarlo, compilarlo y correr los tests.
 */
public record ExecutionRequest(Exercise ejercicio, String studentCode) {
}
