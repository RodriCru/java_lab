package com.lrch.javalab.questions;

import com.lrch.javalab.catalog.CatalogProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

/** Valida el banco real (../questions): que cargue completo, sin duplicados y bien formado. */
class QuestionBankTest {

    private final QuestionBank banco = new QuestionBank(new CatalogProperties("../exercises", "../questions"));

    @Test
    @DisplayName("Carga el banco real con todos sus temas y preguntas validas")
    void cargaElBancoReal() {
        assertThat(banco.size()).isGreaterThanOrEqualTo(300);
        assertThat(banco.all().stream().map(Question::topic).distinct()).hasSize(14);
        assertThat(new HashSet<>(banco.all().stream().map(Question::id).toList())).hasSize(banco.size());
    }

    @Test
    @DisplayName("Cada pregunta tiene 4 opciones, respuesta en rango y explicacion")
    void cadaPreguntaEstaBienFormada() {
        for (Question q : banco.all()) {
            assertThat(q.problemas()).as(q.id()).isEmpty();
            assertThat(q.options()).as(q.id()).hasSize(4);
            assertThat(q.id()).as(q.id()).startsWith(q.topic() + "-");
        }
    }
}
