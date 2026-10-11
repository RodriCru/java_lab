package com.lrch.javalab.admin;

import jakarta.validation.constraints.NotNull;

public record SetActiveRequest(@NotNull(message = "active es obligatorio") Boolean active) {
}
