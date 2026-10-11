package com.lrch.javalab.runner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extrae del codigo de un test el cuerpo del metodo, que es lo que muestra "la entrada". */
final class TestSourceInspector {

    private TestSourceInspector() {
    }

    /** Devuelve el cuerpo del metodo {@code nombre}, sin llaves y sin sangria; null si no lo halla. */
    static String cuerpoDe(String testCode, String nombre) {
        if (testCode == null || nombre == null) {
            return null;
        }
        Matcher m = Pattern.compile("void\\s+" + Pattern.quote(nombre) + "\\s*\\(\\s*\\)[^{]*\\{").matcher(testCode);
        if (!m.find()) {
            return null;
        }
        int inicio = m.end();
        int profundidad = 1;
        for (int i = inicio; i < testCode.length(); i++) {
            char c = testCode.charAt(i);
            if (c == '{') {
                profundidad++;
            } else if (c == '}' && --profundidad == 0) {
                return quitarSangria(testCode.substring(inicio, i));
            }
        }
        return null;
    }

    private static String quitarSangria(String cuerpo) {
        String[] lineas = cuerpo.strip().split("\n");
        int minimo = Integer.MAX_VALUE;
        for (int i = 1; i < lineas.length; i++) {
            if (!lineas[i].isBlank()) {
                minimo = Math.min(minimo, lineas[i].length() - lineas[i].stripLeading().length());
            }
        }
        if (minimo == Integer.MAX_VALUE) {
            return lineas[0].strip();
        }
        StringBuilder sb = new StringBuilder(lineas[0].strip());
        for (int i = 1; i < lineas.length; i++) {
            sb.append('\n').append(lineas[i].length() >= minimo ? lineas[i].substring(minimo) : lineas[i].strip());
        }
        return sb.toString();
    }
}
