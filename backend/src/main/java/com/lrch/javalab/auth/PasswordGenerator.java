package com.lrch.javalab.auth;

import java.security.SecureRandom;

/** Contrasenas temporales para las invitaciones: aleatorias y sin caracteres que se confundan (0/O, 1/l/I). */
public final class PasswordGenerator {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int LONGITUD = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {
    }

    public static String generar() {
        StringBuilder sb = new StringBuilder(LONGITUD);
        for (int i = 0; i < LONGITUD; i++) {
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
