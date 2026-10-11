package com.lrch.javalab.questions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** API del banco de preguntas con un banco de prueba fijo (src/test/resources/preguntas-prueba). */
@SpringBootTest
@AutoConfigureMockMvc
class QuestionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        Path db = Path.of("target", "pruebas-preguntas.db").toAbsolutePath();
        try {
            Files.deleteIfExists(db);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db);
        registry.add("javalab.catalog.questions-dir",
                () -> Path.of("src", "test", "resources", "preguntas-prueba").toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Exige sesion para consultar el banco")
    void exigeSesion() throws Exception {
        mockMvc.perform(get("/api/v1/questions")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Lista las preguntas sin filtrar la respuesta correcta ni la explicacion")
    void listaSinFiltrarLaRespuesta() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/v1/questions").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.pagination.total").value(3))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andReturn();

        String cuerpo = resultado.getResponse().getContentAsString();
        assertThat(cuerpo).doesNotContain("correctIndex").doesNotContain("explanation");
    }

    @Test
    @DisplayName("Filtra por tema y dificultad, y resume los temas")
    void filtraYResumeTemas() throws Exception {
        Cookie sesion = sesion();
        mockMvc.perform(get("/api/v1/questions?topic=demo&difficulty=hard").cookie(sesion))
                .andExpect(jsonPath("$.pagination.total").value(1))
                .andExpect(jsonPath("$.data[0].id").value("demo-02"))
                .andExpect(jsonPath("$.data[0].code").exists());

        mockMvc.perform(get("/api/v1/questions/topics").cookie(sesion))
                .andExpect(jsonPath("$.data[0].topic").value("demo"))
                .andExpect(jsonPath("$.data[0].total").value(2))
                .andExpect(jsonPath("$.data[1].topic").value("otro"));
    }

    @Test
    @DisplayName("Responder revela la solucion y actualiza el estado y el avance por usuario")
    void responderActualizaElProgreso() throws Exception {
        Cookie sesion = sesion();

        mockMvc.perform(answer("demo-01", 0, sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct").value(false))
                .andExpect(jsonPath("$.data.correctIndex").value(1))
                .andExpect(jsonPath("$.data.explanation").value("Porque 1 + 1 = 2."));

        mockMvc.perform(get("/api/v1/questions?status=FAILED").cookie(sesion))
                .andExpect(jsonPath("$.pagination.total").value(1));

        mockMvc.perform(answer("demo-01", 1, sesion))
                .andExpect(jsonPath("$.data.correct").value(true));

        mockMvc.perform(get("/api/v1/questions?topic=demo&status=CORRECT").cookie(sesion))
                .andExpect(jsonPath("$.pagination.total").value(1));
        mockMvc.perform(get("/api/v1/questions/topics").cookie(sesion))
                .andExpect(jsonPath("$.data[0].answered").value(1))
                .andExpect(jsonPath("$.data[0].correct").value(1));

        // Otro usuario no ve el progreso del primero.
        mockMvc.perform(get("/api/v1/questions/topics").cookie(sesion()))
                .andExpect(jsonPath("$.data[0].answered").value(0));
    }

    @Test
    @DisplayName("Rechaza respuestas fuera de rango y preguntas inexistentes con el contrato de error")
    void validaLaRespuesta() throws Exception {
        Cookie sesion = sesion();
        mockMvc.perform(answer("demo-01", 9, sesion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("RESPUESTA_INVALIDA"));
        mockMvc.perform(answer("no-existe", 0, sesion))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PREGUNTA_NO_ENCONTRADA"));
    }

    private org.springframework.test.web.servlet.RequestBuilder answer(String id, int indice, Cookie sesion)
            throws Exception {
        return post("/api/v1/questions/" + id + "/answer")
                .cookie(sesion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("selectedIndex", indice)));
    }

    private Cookie sesion() throws Exception {
        String correo = "preguntas-" + UUID.randomUUID() + "@javalab.test";
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", correo, "password", "secreta123"))))
                .andExpect(status().isCreated())
                .andReturn();
        Cookie sesion = resultado.getResponse().getCookie("javalab_token");
        assertThat(sesion).isNotNull();
        return sesion;
    }
}
