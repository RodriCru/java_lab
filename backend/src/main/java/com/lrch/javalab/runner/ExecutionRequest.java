package com.lrch.javalab.runner;

import com.lrch.javalab.catalog.Exercise;
import com.lrch.javalab.runner.lang.LanguageAdapter;

/**
 * Lo que se va a compilar y ejecutar: el ejercicio, el codigo del alumno y el adaptador del lenguaje.
 *
 * <p>El adaptador decide como se valida el codigo y con que imagen se ejecuta; el ejercicio aporta
 * los tests. Se entrega el {@link Exercise} completo (no solo sus tests) porque cada lenguaje puede
 * necesitar mas datos (la clase esperada en Java, por ejemplo).
 */
public record ExecutionRequest(Exercise ejercicio, LanguageAdapter adapter, String studentCode) {
}
