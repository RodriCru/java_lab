package com.lrch.javalab.auth;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registry) {
        Path db = Path.of("target", "pruebas-auth.db").toAbsolutePath();
        try {
            Files.deleteIfExists(db);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db);
    }

    @Test
    @DisplayName("El registro crea la cuenta y deja la sesion en una cookie httpOnly")
    void registroCreaLaCuentaYSesion() throws Exception {
        String correo = correoNuevo();

        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "secreta123", "displayName", "Luis"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(correo))
                .andExpect(jsonPath("$.data.displayName").value("Luis"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(cookie().exists("javalab_token"))
                .andReturn();

        String setCookie = resultado.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("HttpOnly").contains("SameSite=Lax").contains("Path=/");
    }

    @Test
    @DisplayName("Sin sesion el catalogo responde 401 con el contrato de error")
    void sinSesionElCatalogoResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/levels"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("SESION_REQUERIDA"));
    }

    @Test
    @DisplayName("El endpoint de salud es publico")
    void elEndpointDeSaludEsPublico() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ok"));
    }

    @Test
    @DisplayName("Con sesion se puede consultar el perfil")
    void conSesionSeConsultaElPerfil() throws Exception {
        String correo = correoNuevo();
        Cookie sesion = registrar(correo);

        mockMvc.perform(get("/api/v1/auth/me").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(correo));
    }

    @Test
    @DisplayName("El login devuelve una nueva sesion valida")
    void elLoginDevuelveSesion() throws Exception {
        String correo = correoNuevo();
        registrar(correo);

        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "secreta123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(correo))
                .andReturn();

        Cookie sesion = resultado.getResponse().getCookie("javalab_token");
        assertThat(sesion).isNotNull();

        mockMvc.perform(get("/api/v1/auth/me").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(correo));
    }

    @Test
    @DisplayName("El login con contrasena incorrecta devuelve 401")
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        String correo = correoNuevo();
        registrar(correo);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "contrasena-mala"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    @DisplayName("No se puede registrar dos veces el mismo correo")
    void noSePuedeRegistrarElMismoCorreo() throws Exception {
        String correo = correoNuevo();
        registrar(correo);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "secreta123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_YA_REGISTRADO"));
    }

    @Test
    @DisplayName("Una contrasena corta se rechaza con el detalle del campo")
    void unaContrasenaCortaSeRechaza() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correoNuevo(), "password", "corta"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("contrase")));
    }

    @Test
    @DisplayName("Los acentos viajan en UTF-8")
    void losAcentosViajanEnUtf8() throws Exception {
        String correo = correoNuevo();
        registrar(correo);

        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "secreta123"))))
                .andExpect(status().isOk())
                .andReturn();

        String cuerpo = new String(resultado.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(cuerpo).contains("Sesión iniciada.");
    }

    @Test
    @DisplayName("Un correo con formato invalido se rechaza")
    void unCorreoInvalidoSeRechaza() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "no-es-un-correo", "password", "secreta123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DATOS_INVALIDOS"));
    }

    @Test
    @DisplayName("El logout borra la cookie de sesion")
    void elLogoutBorraLaCookie() throws Exception {
        Cookie sesion = registrar(correoNuevo());

        mockMvc.perform(post("/api/v1/auth/logout").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("javalab_token", 0));
    }

    @Test
    @DisplayName("Un token manipulado no autentica")
    void unTokenManipuladoNoAutentica() throws Exception {
        Cookie falsa = new Cookie("javalab_token", "esto.no.es.un.jwt");

        mockMvc.perform(get("/api/v1/auth/me").cookie(falsa))
                .andExpect(status().isUnauthorized());
    }

    private Cookie registrar(String correo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "secreta123"))))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie sesion = resultado.getResponse().getCookie("javalab_token");
        assertThat(sesion).as("cookie de sesion").isNotNull();
        return sesion;
    }

    private String correoNuevo() {
        return "prueba-" + UUID.randomUUID() + "@javalab.test";
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
