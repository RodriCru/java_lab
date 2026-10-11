package com.lrch.javalab.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
        @NotBlank(message = "el ejercicio es obligatorio") String exerciseSlug,
        @NotBlank(message = "el codigo no puede estar vacio")
        @Size(max = 20_000, message = "el codigo es demasiado largo")
        String sourceCode,
        Integer complexityAnswer) {
}
