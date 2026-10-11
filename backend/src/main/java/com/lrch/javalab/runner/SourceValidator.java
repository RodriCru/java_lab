package com.lrch.javalab.runner;

import com.lrch.javalab.common.ApiException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Revisa el codigo del alumno ANTES de compilarlo.
 *
 * <p>No es la unica defensa: el contenedor corre sin red, sin capacidades y con el sistema de
 * archivos de solo lectura. Aqui se filtra lo evidente para dar un mensaje claro al alumno y
 * para evitar que se llegue a ejecutar codigo que no tiene nada que hacer en un ejercicio.
 */
@Component
public class SourceValidator {

    private static final int MAX_LENGTH = 20_000;

    /** Solo se permite usar java.util y java.math; java.lang no necesita import. */
    private static final List<String> PREFIJOS_PERMITIDOS = List.of("java.util", "java.math", "java.lang");

    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;");

    /** Leer de la entrada estandar no tiene sentido en un ejercicio; Scanner sobre un String si se permite. */
    private static final Pattern ENTRADA_ESTANDAR = Pattern.compile("\\bSystem\\s*\\.\\s*in\\b");

    private static final List<String> TOKENS_PROHIBIDOS = List.of(
            "Runtime",
            "ProcessBuilder",
            "ProcessHandle",
            "System.exit",
            "System.getenv",
            "System.getProperties",
            "System.getProperty",
            "System.setProperty",
            "System.load",
            "Class.forName",
            "getClassLoader",
            "java.io.",
            "java.nio.file",
            "java.net.",
            "java.lang.reflect",
            "java.lang.invoke",
            "MethodHandle",
            "ClassLoader",
            "ModuleLayer",
            "sun.",
            "jdk.internal",
            "Unsafe",
            "Thread",
            "Executor",
            "Socket",
            "ServerSocket",
            "FileWriter",
            "FileReader",
            "FileInputStream",
            "FileOutputStream",
            "RandomAccessFile",
            "SecurityManager",
            "addShutdownHook");

    public void validar(String codigo, String classNameEsperado) {
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
            String paquete = matcher.group(1);
            boolean permitido = PREFIJOS_PERMITIDOS.stream().anyMatch(paquete::startsWith);
            if (!permitido) {
                problemas.add("import " + paquete + " no está permitido");
            }
        }

        // RuntimeException es legitima (ejercicios de excepciones): se ignora antes de buscar "Runtime".
        String codigoSinExcepciones = codigo.replace("RuntimeException", "");
        for (String token : TOKENS_PROHIBIDOS) {
            if (codigoSinExcepciones.contains(token)) {
                problemas.add("no está permitido usar '" + token + "'");
            }
        }

        if (ENTRADA_ESTANDAR.matcher(codigo).find()) {
            problemas.add("no está permitido leer de 'System.in' (usa Scanner sobre un String)");
        }

        if (!declaraClase(codigo, classNameEsperado)) {
            problemas.add("tu código debe declarar la clase '" + classNameEsperado + "'");
        }

        if (!problemas.isEmpty()) {
            throw ApiException.badRequest("CODIGO_NO_PERMITIDO",
                    "No se puede ejecutar tu código: " + String.join("; ", problemas) + ".");
        }
    }

    private boolean declaraClase(String codigo, String className) {
        return Pattern.compile("\\bclass\\s+" + Pattern.quote(className) + "\\b").matcher(codigo).find();
    }
}
