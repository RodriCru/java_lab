package com.lrch.javalab.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del correo saliente. Sin {@code resendApiKey} los correos no se envian: el contenido
 * (con el codigo OTP) se escribe en el log del backend.
 */
@ConfigurationProperties(prefix = "javalab.mail")
public record MailProperties(
        String resendApiKey,
        String from,
        Boolean async) {

    public MailProperties {
        if (from == null || from.isBlank()) {
            from = "Java Lab <no-reply@mail.luisrodrigoch.lat>";
        }
    }

    public boolean tieneResend() {
        return resendApiKey != null && !resendApiKey.isBlank();
    }

    /** Por defecto el envio es asincrono para no frenar la respuesta ni delatar si el correo existe. */
    public boolean esAsincrono() {
        return async == null || async;
    }
}
