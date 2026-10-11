package com.lrch.javalab.mail;

/** Correo listo para enviar: version HTML con estilos y version de texto plano. */
public record EmailMessage(String to, String subject, String html, String text) {
}
