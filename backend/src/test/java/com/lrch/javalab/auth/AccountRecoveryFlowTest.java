package com.lrch.javalab.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lrch.javalab.mail.EmailMessage;
import com.lrch.javalab.mail.EmailSender;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountRecoveryFlowTest {

    private static final List<EmailMessage> ENVIADOS = new CopyOnWriteArrayList<>();
    private static final Pattern CODIGO = Pattern.compile("Tu código: (\\d{3}) (\\d{3})");

    @TestConfiguration
    static class CorreoFalso {
        @Bean
        @Primary
        EmailSender correoFalso() {
            return ENVIADOS::add;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configuracion(DynamicPropertyRegistry registry) {
        Path db = Path.of("target", "pruebas-recuperacion.db").toAbsolutePath();
        try {
            Files.deleteIfExists(db);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db);
        registry.add("javalab.mail.async", () -> "false");
    }

    @BeforeEach
    void limpiar() {
        ENVIADOS.clear();
    }

    @Test
    @DisplayName("El registro manda una bienvenida con el correo, nunca con la contraseña")
    void bienvenidaSinContrasena() throws Exception {
        String correo = correoNuevo();
        registrar(correo, "secreta123").andExpect(status().isCreated());

        assertThat(ENVIADOS).hasSize(1);
        EmailMessage bienvenida = ENVIADOS.get(0);
        assertThat(bienvenida.to()).isEqualTo(correo);
        assertThat(bienvenida.text()).contains(correo).doesNotContain("secreta123");
        assertThat(bienvenida.html()).contains(correo).doesNotContain("secreta123");
    }

    @Test
    @DisplayName("Recuperar la contraseña con OTP: cambia la clave y el código solo sirve una vez")
    void recuperacionCompleta() throws Exception {
        String correo = correoNuevo();
        registrar(correo, "secreta123");
        ENVIADOS.clear();

        olvide(correo).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        String codigo = ultimoCodigo(correo);

        restablecer(correo, codigo, "nuevaClave456").andExpect(status().isOk());

        login(correo, "nuevaClave456").andExpect(status().isOk());
        login(correo, "secreta123").andExpect(status().isUnauthorized());

        restablecer(correo, codigo, "otraClave789")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CODIGO_INVALIDO"));
    }

    @Test
    @DisplayName("Un correo que no existe recibe la misma respuesta y no se envía nada")
    void correoInexistenteNoDelata() throws Exception {
        olvide(correoNuevo())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si el correo tiene una cuenta, te enviamos un código de 6 dígitos."));
        assertThat(ENVIADOS).isEmpty();

        restablecer(correoNuevo(), "123456", "nuevaClave456")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CODIGO_INVALIDO"));
    }

    @Test
    @DisplayName("Tras 5 intentos fallidos el código muere, aunque después se escriba el correcto")
    void cincoIntentosInvalidanElCodigo() throws Exception {
        String correo = correoNuevo();
        registrar(correo, "secreta123");
        ENVIADOS.clear();
        olvide(correo);
        String correcto = ultimoCodigo(correo);
        String malo = correcto.equals("000000") ? "000001" : "000000";

        for (int i = 0; i < 5; i++) {
            restablecer(correo, malo, "nuevaClave456")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("CODIGO_INVALIDO"));
        }
        restablecer(correo, correcto, "nuevaClave456")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DEMASIADOS_INTENTOS"));
        login(correo, "secreta123").andExpect(status().isOk());
    }

    @Test
    @DisplayName("Pedir otro código al instante no manda un segundo correo ni delata nada")
    void limiteDeFrecuencia() throws Exception {
        String correo = correoNuevo();
        registrar(correo, "secreta123");
        ENVIADOS.clear();

        olvide(correo).andExpect(status().isOk());
        olvide(correo).andExpect(status().isOk());

        assertThat(ENVIADOS).hasSize(1);
    }

    @Test
    @DisplayName("Cambiar el correo: el código va al correo nuevo y la sesión se reemite con él")
    void cambioDeCorreo() throws Exception {
        String viejo = correoNuevo();
        String nuevo = correoNuevo();
        Cookie sesion = registrar(viejo, "secreta123").andReturn().getResponse().getCookie("javalab_token");
        ENVIADOS.clear();

        pedirCambio(sesion, nuevo, "incorrecta1")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CONTRASENA_INCORRECTA"));
        assertThat(ENVIADOS).isEmpty();

        pedirCambio(sesion, nuevo, "secreta123").andExpect(status().isOk());
        assertThat(ENVIADOS).hasSize(1);
        assertThat(ENVIADOS.get(0).to()).isEqualTo(nuevo);
        String codigo = ultimoCodigo(nuevo);

        MvcResult confirmado = mockMvc.perform(post("/api/v1/auth/me/email/confirm")
                        .cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("code", codigo))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(nuevo))
                .andReturn();

        Cookie sesionNueva = confirmado.getResponse().getCookie("javalab_token");
        assertThat(sesionNueva).isNotNull();
        mockMvc.perform(get("/api/v1/auth/me").cookie(sesionNueva))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(nuevo));

        login(nuevo, "secreta123").andExpect(status().isOk());
        login(viejo, "secreta123").andExpect(status().isUnauthorized());
        assertThat(ENVIADOS.stream().anyMatch(m -> m.to().equals(viejo))).isTrue();
    }

    @Test
    @DisplayName("No se puede cambiar a un correo que ya tiene cuenta")
    void cambioACorreoOcupado() throws Exception {
        String ocupado = correoNuevo();
        registrar(ocupado, "secreta123");
        Cookie sesion = registrar(correoNuevo(), "secreta123").andReturn().getResponse().getCookie("javalab_token");

        pedirCambio(sesion, ocupado, "secreta123")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_YA_REGISTRADO"));
    }

    @Test
    @DisplayName("Los endpoints de cambio de correo exigen sesión")
    void cambioDeCorreoExigeSesion() throws Exception {
        mockMvc.perform(post("/api/v1/auth/me/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("newEmail", correoNuevo(), "currentPassword", "secreta123"))))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions registrar(String correo, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", correo, "password", clave, "displayName", "Luis <b>"))));
    }

    private ResultActions login(String correo, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", correo, "password", clave))));
    }

    private ResultActions olvide(String correo) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", correo))));
    }

    private ResultActions restablecer(String correo, String codigo, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", correo, "code", codigo, "newPassword", clave))));
    }

    private ResultActions pedirCambio(Cookie sesion, String nuevo, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/me/email")
                .cookie(sesion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("newEmail", nuevo, "currentPassword", clave))));
    }

    /** Lee el codigo del ultimo correo enviado a esa direccion (viene como «123 456»). */
    private String ultimoCodigo(String destino) {
        for (int i = ENVIADOS.size() - 1; i >= 0; i--) {
            EmailMessage mensaje = ENVIADOS.get(i);
            Matcher m = CODIGO.matcher(mensaje.text());
            if (mensaje.to().equals(destino) && m.find()) {
                return m.group(1) + m.group(2);
            }
        }
        throw new AssertionError("No se envió ningún código a " + destino);
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    private String correoNuevo() {
        return "u" + UUID.randomUUID().toString().substring(0, 8) + "@javalab.test";
    }
}
