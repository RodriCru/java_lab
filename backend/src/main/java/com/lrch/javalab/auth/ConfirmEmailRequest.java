package com.lrch.javalab.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmEmailRequest(
        @NotBlank(message = "el código es obligatorio")
        @Pattern(regexp = "\\d{6}", message = "el código tiene 6 dígitos")
        String code) {
}
