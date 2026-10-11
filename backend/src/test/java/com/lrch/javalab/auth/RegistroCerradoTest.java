package com.lrch.javalab.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "javalab.auth.registration-enabled=false")
@AutoConfigureMockMvc
class RegistroCerradoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registry) {
        Path db = Path.of("target", "pruebas-registro-cerrado.db").toAbsolutePath();
        try {
            Files.deleteIfExists(db);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db);
    }

    @Test
    @DisplayName("Con el registro cerrado no se pueden crear cuentas nuevas")
    void registroCerrado() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "nuevo@javalab.test", "password", "secreta123"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("REGISTRO_CERRADO"));
    }
}
