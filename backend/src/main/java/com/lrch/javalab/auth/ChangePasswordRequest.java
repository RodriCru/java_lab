package com.lrch.javalab.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "la contraseña actual es obligatoria")
        @Size(max = 72, message = "la contraseña es demasiado larga")
        String currentPassword,

        @NotBlank(message = "la contraseña nueva es obligatoria")
        @Size(min = 8, max = 72, message = "la contraseña debe tener entre 8 y 72 caracteres")
        String newPassword) {
}
