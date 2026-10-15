package com.lrch.javalab.runner.lang;

import com.lrch.javalab.common.ApiException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Revisa el codigo Python del alumno antes de ejecutarlo.
 *
 * <p>Es el equivalente de {@code SourceValidator} para Java: no es la unica defensa (el contenedor
 * corre sin red, sin capacidades y con el sistema de archivos de solo lectura), pero filtra lo
 * evidente para dar un mensaje claro y evitar ejecutar cosas que no tienen nada que hacer aqui.
 */
@Component
public class PythonSourceValidator {

    private static final int MAX_LENGTH = 20_000;

    /** Modulos permitidos; el resto de imports se rechaza. */
    private static final List<String> MODULOS_PERMITIDOS = List.of(
            "math", "random", "string", "collections", "itertools", "functools", "typing",
            "dataclasses", "enum", "abc", "copy", "re", "heapq", "bisect", "decimal",
            "fractions", "statistics", "textwrap", "unicodedata");

    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*(?:import|from)\\s+([A-Za-z_][\\w.]*)");

    /** Llamadas y atributos que no pintan nada en un ejercicio. */
    private static final List<String> TOKENS_PROHIBIDOS = List.of(
            "subprocess", "os.", "import os", "sys.", "import sys", "shutil", "socket",
            "urllib", "http.client", "requests", "ftplib", "smtplib", "telnetlib",
            "ctypes", "multiprocessing", "threading", "asyncio", "signal",
            "eval(", "exec(", "compile(", "__import__", "open(", "input(",
            "globals(", "locals(", "vars(", "getattr(", "setattr(", "delattr(",
            "pickle", "marshal", "pty", "platform", "tempfile", "pathlib");

    public void validar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw ApiException.badRequest("CODIGO_VACIO", "Escribe tu solución antes de enviarla.");
        }
        if (codigo.length() > MAX_LENGTH) {
            throw ApiException.badRequest("CODIGO_DEMASIADO_LARGO",
                    "Tu código supera el límite de " + MAX_LENGTH + " caracteres.");
        }

        List<String> problemas = new ArrayList<>();

        Matcher matcher = IMPORT.matcher(codigo);
        while (matcher.find()) {
            String modulo = matcher.group(1);
            String raiz = modulo.split("\\.")[0];
            boolean permitido = MODULOS_PERMITIDOS.stream().anyMatch(raiz::equals);
            if (!permitido) {
                problemas.add("import " + modulo + " no está permitido");
            }
        }

        String sinDocstrings = quitarCadenas(codigo);
        for (String token : TOKENS_PROHIBIDOS) {
            if (sinDocstrings.contains(token)) {
                problemas.add("no está permitido usar '" + token + "'");
            }
        }

        if (!problemas.isEmpty()) {
            throw ApiException.badRequest("CODIGO_NO_PERMITIDO",
                    "No se puede ejecutar tu código: " + String.join("; ", problemas) + ".");
        }
    }

    /**
     * Quita literales de cadena antes de buscar tokens prohibidos, para no rechazar un codigo por un
     * mensaje que mencione, por ejemplo, "open(" dentro de un texto.
     */
    private String quitarCadenas(String codigo) {
        return codigo
                .replaceAll("(?s)\"\"\".*?\"\"\"", "\"\"")
                .replaceAll("(?s)'''.*?'''", "''")
                .replaceAll("\"(?:[^\"\\\\]|\\\\.)*\"", "\"\"")
                .replaceAll("'(?:[^'\\\\]|\\\\.)*'", "''");
    }
}
