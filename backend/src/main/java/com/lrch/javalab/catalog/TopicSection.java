package com.lrch.javalab.catalog;

/** Sección de un nivel: un tema con su avance dentro de ese nivel. */
public record TopicSection(String id, String name, String description, int total, int solved) {
}
