package com.lrch.javalab.runner.lang;

/**
 * Error de negocio del nucleo de ejecucion: el codigo del alumno no se puede ejecutar (vacio,
 * demasiado largo, con imports prohibidos...). El backend lo convierte a su propia respuesta HTTP
 * (ver {@code ApiException}); asi el nucleo no depende de Spring.
 */
public class RunnerException extends RuntimeException {

    private final String code;

    public RunnerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static RunnerException badRequest(String code, String message) {
        return new RunnerException(code, message);
    }
}
