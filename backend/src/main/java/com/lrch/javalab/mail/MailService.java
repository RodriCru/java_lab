package com.lrch.javalab.mail;

import com.lrch.javalab.auth.CodePurpose;
import com.lrch.javalab.users.User;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Punto unico para mandar correos. Un fallo de envio nunca rompe la operacion que lo origino
 * (registro, recuperacion...): se registra en el log y ya.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final EmailSender sender;
    private final ExecutorService executor;

    public MailService(EmailSender sender, MailProperties properties) {
        this.sender = sender;
        this.executor = properties.esAsincrono()
                ? Executors.newFixedThreadPool(2, runnable -> {
                    Thread hilo = new Thread(runnable, "correo");
                    hilo.setDaemon(true);
                    return hilo;
                })
                : null;
    }

    public void enviarBienvenida(User user) {
        enviar(EmailTemplates.bienvenida(user.getEmail(), user.getDisplayName(), user.getEmail()));
    }

    public void enviarInvitacion(User user, String passwordTemporal) {
        enviar(EmailTemplates.invitacion(user.getEmail(), user.getDisplayName(), user.getEmail(), passwordTemporal));
    }

    public void enviarCodigo(String destino, String nombre, CodePurpose proposito, String codigo) {
        enviar(proposito == CodePurpose.PASSWORD_RESET
                ? EmailTemplates.codigoRecuperacion(destino, nombre, codigo)
                : EmailTemplates.codigoCambioCorreo(destino, nombre, codigo));
    }

    public void enviarAvisoCambioCorreo(String correoAnterior, String nombre, String correoNuevo) {
        enviar(EmailTemplates.avisoCambioCorreo(correoAnterior, nombre, correoNuevo));
    }

    private void enviar(EmailMessage mensaje) {
        if (executor == null) {
            enviarSinFallar(mensaje);
        } else {
            executor.execute(() -> enviarSinFallar(mensaje));
        }
    }

    private void enviarSinFallar(EmailMessage mensaje) {
        try {
            sender.send(mensaje);
        } catch (Exception ex) {
            log.warn("No se pudo enviar el correo '{}' a {}: {}", mensaje.subject(), mensaje.to(), ex.getMessage());
        }
    }

    @PreDestroy
    void cerrar() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
