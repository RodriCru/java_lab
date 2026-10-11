package com.lrch.javalab.auth;

/** Identidad autenticada que viaja en el SecurityContext. */
public record AuthenticatedUser(Long id, String email, String displayName) {
}
