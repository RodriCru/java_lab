package com.lrch.javalab.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "el correo no tiene un formato válido")
        @Size(max = 254, message = "el correo es demasiado largo")
        String email) {
}
