package com.lrch.javalab.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Respaldo para desarrollo: sin API key de Resend el correo (y su codigo OTP) sale en el log. */
public class LogEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    public void send(EmailMessage message) {
        log.info("""

                ======== CORREO SIMULADO (falta JAVALAB_RESEND_API_KEY) ========
                Para:   {}
                Asunto: {}
                ----------------------------------------------------------------
                {}
                ================================================================""",
                message.to(), message.subject(), message.text());
    }
}
