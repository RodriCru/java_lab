package com.lrch.javalab.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    public EmailSender emailSender(MailProperties properties) {
        if (properties.tieneResend()) {
            log.info("Correo: enviando con Resend desde {}", properties.from());
            return new ResendEmailSender(properties);
        }
        log.warn("Correo: JAVALAB_RESEND_API_KEY no está definida; los correos y sus códigos saldrán en este log.");
        return new LogEmailSender();
    }
}
