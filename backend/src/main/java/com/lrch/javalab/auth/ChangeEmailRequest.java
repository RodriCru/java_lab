package com.lrch.javalab.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailRequest(
        @NotBlank(message = "el correo nuevo es obligatorio")
        @Email(message = "el correo no tiene un formato válido")
        @Size(max = 254, message = "el correo es demasiado largo")
        String newEmail,

        @NotBlank(message = "la contraseña actual es obligatoria")
        @Size(max = 72, message = "la contraseña es demasiado larga")
        String currentPassword) {
}
