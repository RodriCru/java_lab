package com.lrch.javalab.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Codigo OTP de un solo uso. Solo se guarda su HMAC, nunca el codigo en claro. */
@Entity
@Table(name = "email_codes")
public class EmailCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CodePurpose purpose;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    /** Correo al que se envio el codigo (en un cambio de correo, el correo nuevo). */
    @Column(name = "target_email", nullable = false)
    private String targetEmail;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "expires_at", nullable = false)
    private long expiresAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "used_at")
    private Long usedAt;

    protected EmailCode() {
    }

    public EmailCode(Long userId, CodePurpose purpose, String codeHash, String targetEmail,
                     long createdAt, long expiresAt) {
        this.userId = userId;
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.targetEmail = targetEmail;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void registrarIntentoFallido() {
        this.attempts++;
    }

    public Long getUsedAt() {
        return usedAt;
    }

    public void marcarUsado(long ahora) {
        this.usedAt = ahora;
    }
}
