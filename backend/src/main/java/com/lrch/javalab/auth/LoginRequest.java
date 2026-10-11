package com.lrch.javalab.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "el correo es obligatorio") String email,
        @NotBlank(message = "la contraseña es obligatoria") String password) {
}
