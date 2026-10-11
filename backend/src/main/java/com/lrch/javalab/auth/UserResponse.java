package com.lrch.javalab.auth;

import com.lrch.javalab.users.User;

import java.time.Instant;

/** Datos publicos del usuario: nunca se expone el hash de la contrasena. */
public record UserResponse(Long id, String email, String displayName, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
    }
}
