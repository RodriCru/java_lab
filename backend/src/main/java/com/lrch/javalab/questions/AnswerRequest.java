package com.lrch.javalab.questions;

/** Cuerpo de {@code POST /questions/{id}/answer}: el indice de la opcion elegida. */
public record AnswerRequest(Integer selectedIndex) {
}
