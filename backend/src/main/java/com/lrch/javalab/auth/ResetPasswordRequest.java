package com.lrch.javalab.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "el correo no tiene un formato válido")
        @Size(max = 254, message = "el correo es demasiado largo")
        String email,

        @NotBlank(message = "el código es obligatorio")
        @Pattern(regexp = "\\d{6}", message = "el código tiene 6 dígitos")
        String code,

        @NotBlank(message = "la contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "la contraseña debe tener entre 8 y 72 caracteres")
        String newPassword) {
}
