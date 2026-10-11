package com.lrch.javalab.catalog;

/** Tema del catálogo: nombre, descripción, posición y teoría (Markdown, puede ser nula). */
public record TopicInfo(String id, String name, String description, int order, String theory) {
}
