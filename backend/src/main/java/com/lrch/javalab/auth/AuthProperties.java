package com.lrch.javalab.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "javalab.auth")
public record AuthProperties(
        String jwtSecret,
        long jwtExpirationMinutes,
        String cookieName,
        String allowedOrigins,
        Boolean cookieSecure,
        Boolean registrationEnabled,
        String superAdminEmail) {

    public AuthProperties {
        if (cookieName == null || cookieName.isBlank()) {
            cookieName = "javalab_token";
        }
        if (jwtExpirationMinutes <= 0) {
            jwtExpirationMinutes = 1440;
        }
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            allowedOrigins = "http://localhost:3000";
        }
    }

    /** En produccion debe ser true (la cookie solo viaja por HTTPS). */
    public boolean isCookieSecure() {
        return Boolean.TRUE.equals(cookieSecure);
    }

    /** Si es false, nadie puede crear cuentas nuevas (el login de las existentes sigue funcionando). */
    public boolean isRegistrationEnabled() {
        return registrationEnabled == null || registrationEnabled;
    }

    public List<String> originList() {
        return List.of(allowedOrigins.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
