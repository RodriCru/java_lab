package com.lrch.javalab.mail;

/** Salida de correo. Si el envio falla lanza una excepcion; {@link MailService} la atrapa. */
public interface EmailSender {

    void send(EmailMessage message);
}
