package com.lrch.javalab.runner.lang;

/**
 * Lo minimo que un {@link LanguageAdapter} necesita saber de un ejercicio: su lenguaje, la clase
 * esperada (solo en Java) y el codigo de los tests. Es un tipo propio del nucleo para no arrastrar
 * el modelo del catalogo (con Jackson) al runner-service.
 */
public record RunnerExercise(String language, String className, String tests) {
}
