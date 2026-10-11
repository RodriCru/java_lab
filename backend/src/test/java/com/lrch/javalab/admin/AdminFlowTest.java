package com.lrch.javalab.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lrch.javalab.auth.AuthProperties;
import com.lrch.javalab.mail.EmailMessage;
import com.lrch.javalab.mail.EmailSender;
import com.lrch.javalab.users.User;
import com.lrch.javalab.users.UserRepository;
import com.lrch.javalab.users.UserRole;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
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
class AdminFlowTest {

    private static final List<EmailMessage> ENVIADOS = new CopyOnWriteArrayList<>();
    private static final Pattern TEMPORAL = Pattern.compile("Contraseña temporal: (\\S+)");

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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void configuracion(DynamicPropertyRegistry registry) {
        Path db = Path.of("target", "pruebas-admin.db").toAbsolutePath();
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
    @DisplayName("Un usuario normal no puede usar el panel de administración")
    void usuarioNormalNoEntra() throws Exception {
        Cookie normal = sesion(crearUsuario(UserRole.USER), "secreta123");

        mockMvc.perform(get("/api/v1/admin/users").cookie(normal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
        mockMvc.perform(post("/api/v1/admin/invitations").cookie(normal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo()))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invitar crea la cuenta, manda la contraseña temporal por correo y no la devuelve en la API")
    void invitarEnviaContrasenaTemporal() throws Exception {
        Cookie admin = sesion(crearUsuario(UserRole.SUPER_ADMIN), "secreta123");
        String invitado = correo();
        ENVIADOS.clear();

        String respuesta = mockMvc.perform(post("/api/v1/admin/invitations").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", invitado, "displayName", "Ana"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(invitado))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        assertThat(ENVIADOS).hasSize(1);
        EmailMessage invitacion = ENVIADOS.get(0);
        assertThat(invitacion.to()).isEqualTo(invitado);
        assertThat(invitacion.text()).contains("cambiarla");
        String temporal = temporalDe(invitacion);
        assertThat(temporal).hasSize(12);
        assertThat(respuesta).doesNotContain(temporal).doesNotContainIgnoringCase("password");

        login(invitado, temporal).andExpect(status().isOk());

        String listado = mockMvc.perform(get("/api/v1/admin/users").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.email=='" + invitado + "')]").isNotEmpty())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        // El listado trae el avance por nivel (porcentaje de cada uno).
        assertThat(listado).contains("\"levels\":[{\"level\":0").contains("\"percent\":0");
    }

    @Test
    @DisplayName("No se puede invitar dos veces al mismo correo")
    void correoRepetido() throws Exception {
        Cookie admin = sesion(crearUsuario(UserRole.SUPER_ADMIN), "secreta123");
        String invitado = correo();
        invitar(admin, invitado).andExpect(status().isCreated());
        invitar(admin, invitado).andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_YA_REGISTRADO"));
    }

    @Test
    @DisplayName("Reenviar genera otra contraseña temporal y la anterior deja de servir")
    void reenviarInvitacion() throws Exception {
        Cookie admin = sesion(crearUsuario(UserRole.SUPER_ADMIN), "secreta123");
        String invitado = correo();
        invitar(admin, invitado).andExpect(status().isCreated());
        String primera = temporalDe(ENVIADOS.get(ENVIADOS.size() - 1));
        Long id = userRepository.findByEmailIgnoreCase(invitado).orElseThrow().getId();

        mockMvc.perform(post("/api/v1/admin/users/" + id + "/resend-invitation").cookie(admin))
                .andExpect(status().isOk());
        String segunda = temporalDe(ENVIADOS.get(ENVIADOS.size() - 1));

        assertThat(segunda).isNotEqualTo(primera);
        login(invitado, primera).andExpect(status().isUnauthorized());
        login(invitado, segunda).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Desactivar bloquea el acceso, reactivar lo devuelve y un super admin no se puede desactivar")
    void desactivarYReactivar() throws Exception {
        User adminUser = crearUsuario(UserRole.SUPER_ADMIN);
        Cookie admin = sesion(adminUser, "secreta123");
        String invitado = correo();
        invitar(admin, invitado);
        String temporal = temporalDe(ENVIADOS.get(ENVIADOS.size() - 1));
        Long id = userRepository.findByEmailIgnoreCase(invitado).orElseThrow().getId();

        mockMvc.perform(post("/api/v1/admin/users/" + id + "/active").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("active", false))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.active").value(false));
        login(invitado, temporal).andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/users/" + id + "/active").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("active", true))))
                .andExpect(status().isOk());
        login(invitado, temporal).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/users/" + adminUser.getId() + "/active").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("active", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));
    }

    @Test
    @DisplayName("El invitado puede cambiar su contraseña temporal desde su perfil")
    void cambiarContrasena() throws Exception {
        Cookie admin = sesion(crearUsuario(UserRole.SUPER_ADMIN), "secreta123");
        String invitado = correo();
        invitar(admin, invitado);
        String temporal = temporalDe(ENVIADOS.get(ENVIADOS.size() - 1));
        Cookie sesionInvitado = login(invitado, temporal).andReturn().getResponse().getCookie("javalab_token");

        mockMvc.perform(post("/api/v1/auth/me/password").cookie(sesionInvitado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", "incorrecta1", "newPassword", "miClaveNueva1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CONTRASENA_INCORRECTA"));

        mockMvc.perform(post("/api/v1/auth/me/password").cookie(sesionInvitado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", temporal, "newPassword", "miClaveNueva1"))))
                .andExpect(status().isOk());

        login(invitado, temporal).andExpect(status().isUnauthorized());
        login(invitado, "miClaveNueva1").andExpect(status().isOk());
    }

    @Test
    @DisplayName("Mandar role/admin en el JSON de registro no da privilegios")
    void roleEnElJsonSeIgnora() throws Exception {
        String correo = correo();
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "secreta123",
                                "role", "SUPER_ADMIN", "admin", true, "active", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("USER"));

        assertThat(userRepository.findByEmailIgnoreCase(correo).orElseThrow().getRole()).isEqualTo(UserRole.USER);
        Cookie sesion = login(correo, "secreta123").andReturn().getResponse().getCookie("javalab_token");
        mockMvc.perform(get("/api/v1/admin/users").cookie(sesion)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un token firmado con otra clave no sirve, aunque diga ser un super admin")
    void tokenFalsificadoNoSirve() throws Exception {
        User admin = crearUsuario(UserRole.SUPER_ADMIN);
        String falso = io.jsonwebtoken.Jwts.builder()
                .subject(String.valueOf(admin.getId()))
                .claim("email", admin.getEmail())
                .claim("role", "SUPER_ADMIN")
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "una-clave-que-no-es-la-del-servidor-0123456789".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/v1/admin/users").cookie(new Cookie("javalab_token", falso)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El estado del registro es público y refleja la configuración")
    void estadoDelRegistro() throws Exception {
        mockMvc.perform(get("/api/v1/auth/registration-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    @DisplayName("JAVALAB_SUPER_ADMIN_EMAIL promueve una cuenta existente y no crea cuentas")
    void bootstrapPromueve() throws Exception {
        User existente = crearUsuario(UserRole.USER);
        AuthProperties propiedades = new AuthProperties("x".repeat(40), 60, null, null, null, null,
                existente.getEmail());

        new SuperAdminBootstrap(propiedades, userRepository).run(null);

        assertThat(userRepository.findById(existente.getId()).orElseThrow().getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        long antes = userRepository.count();
        new SuperAdminBootstrap(new AuthProperties("x".repeat(40), 60, null, null, null, null, "no-existe@javalab.test"),
                userRepository).run(null);
        assertThat(userRepository.count()).isEqualTo(antes);
    }

    private User crearUsuario(UserRole rol) {
        User user = new User(correo(), passwordEncoder.encode("secreta123"), "Prueba");
        user.setRole(rol);
        return userRepository.save(user);
    }

    private Cookie sesion(User user, String clave) throws Exception {
        return login(user.getEmail(), clave).andReturn().getResponse().getCookie("javalab_token");
    }

    private ResultActions login(String correo, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", correo, "password", clave))));
    }

    private ResultActions invitar(Cookie admin, String invitado) throws Exception {
        return mockMvc.perform(post("/api/v1/admin/invitations").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("email", invitado))));
    }

    private String temporalDe(EmailMessage mensaje) {
        Matcher m = TEMPORAL.matcher(mensaje.text());
        assertThat(m.find()).as("el correo trae la contraseña temporal").isTrue();
        return m.group(1);
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    private String correo() {
        return "u" + UUID.randomUUID().toString().substring(0, 8) + "@javalab.test";
    }
}
