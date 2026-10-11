package com.lrch.javalab.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(AuthProperties properties) {
        byte[] secret = properties.jwtSecret() == null
                ? new byte[0]
                : properties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                    "JAVALAB_JWT_SECRET debe tener al menos 32 caracteres. Longitud actual: " + secret.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(secret);
        this.expirationMillis = properties.jwtExpirationMinutes() * 60_000L;
    }

    public String generateToken(Long userId, String email) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expirationMillis)))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Long> extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT invalido: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public long expirationMillis() {
        return expirationMillis;
    }
}
