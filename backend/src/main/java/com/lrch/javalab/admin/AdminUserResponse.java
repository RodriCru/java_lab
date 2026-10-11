package com.lrch.javalab.admin;

import com.lrch.javalab.users.User;
import com.lrch.javalab.users.UserRole;

import java.time.Instant;

/** Vista de un usuario para el panel de administracion. Nunca incluye el hash de la contrasena. */
public record AdminUserResponse(Long id, String email, String displayName, UserRole role, boolean active,
                                Instant createdAt, Long lastLoginAt) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(),
                user.isActive(), user.getCreatedAt(), user.getLastLoginAt());
    }
}
