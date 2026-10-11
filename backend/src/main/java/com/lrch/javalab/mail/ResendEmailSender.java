package com.lrch.javalab.mail;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Envia por la API HTTP de Resend (https://resend.com/docs/api-reference/emails/send-email). */
public class ResendEmailSender implements EmailSender {

    private final RestClient client;
    private final String from;

    public ResendEmailSender(MailProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        this.client = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + properties.resendApiKey())
                .build();
        this.from = properties.from();
    }

    @Override
    public void send(EmailMessage message) {
        client.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", from,
                        "to", List.of(message.to()),
                        "subject", message.subject(),
                        "html", message.html(),
                        "text", message.text()))
                .retrieve()
                .toBodilessEntity();
    }
}
