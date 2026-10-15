package com.lrch.javalab.runner.lang;

import com.lrch.javalab.catalog.Exercise;

import java.nio.file.Path;

/**
 * Todo lo que cambia de un lenguaje de programacion a otro en el motor de ejecucion.
 *
 * <p>El resto del motor (contenedor, limites, timeout, lectura de resultados) es comun: cada
 * lenguaje solo aporta como se valida el codigo del alumno, como se escriben los archivos que
 * entran al contenedor y con que imagen se ejecuta.
 *
 * <p>Java, Python y TypeScript generan un reporte XML compatible con JUnit, asi que comparten
 * el mismo {@code JUnitReportParser}. C y C++ necesitaran su propio formato de resultados.
 */
public interface LanguageAdapter {

    /** Identificador del lenguaje, en minusculas: {@code java}, {@code python}... */
    String language();

    /** Nombre de la imagen Docker que compila y ejecuta este lenguaje. */
    String image();

    /**
     * Revisa el codigo del alumno antes de compilarlo y lanza {@code ApiException} con un mensaje
     * claro si no se puede ejecutar. Cada lenguaje define sus propias reglas.
     */
    void validar(String codigo, Exercise ejercicio);

    /**
     * Escribe los archivos que el contenedor encontrara en {@code /in} (solo lectura): el codigo del
     * alumno y los tests del ejercicio. Devuelve cuantos archivos escribio (para validar que no falto nada).
     */
    int escribirEntrada(Path srcDir, Exercise ejercicio, String codigoAlumno);
}
