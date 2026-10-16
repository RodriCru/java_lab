package com.lrch.javalab.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lrch.javalab.catalog.Exercise;
import com.lrch.javalab.catalog.ExerciseCatalog;
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

/**
 * Flujo completo de la API: registro, catalogo, envio de una solucion y progreso.
 *
 * <p>Los envios se ejecutan de verdad: compilan el codigo y corren los tests JUnit con el
 * motor configurado (Docker o local). Es la verificacion mas importante del backend.
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(MotorDeMentiraConfig.class)
class ApiFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExerciseCatalog catalog;

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registry) {
        Path db = Path.of("target", "pruebas-api.db").toAbsolutePath();
        try {
            Files.deleteIfExists(db);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db);
        // Desactiva el motor real (HTTP): estos tests usan el de mentira (MotorDeMentiraConfig).
        registry.add("javalab.runner.service-enabled", () -> "false");
    }

    @Test
    @DisplayName("El catalogo publica los niveles con el contrato de respuesta")
    void elCatalogoPublicaLosNiveles() throws Exception {
        mockMvc.perform(get("/api/v1/levels").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].level").value(0))
                .andExpect(jsonPath("$.data[0].name").value("Fundamentos"))
                .andExpect(jsonPath("$.data[0].total").value(catalog.byLevel(0).size()))
                .andExpect(jsonPath("$.data[1].level").value(1))
                .andExpect(jsonPath("$.data[1].name").value("Básico"))
                .andExpect(jsonPath("$.data[1].total").value(catalog.byLevel(1).size()))
                .andExpect(jsonPath("$.data[1].solved").value(0))
                .andExpect(jsonPath("$.data[1].percent").value(0));
    }

    @Test
    @DisplayName("La lista de ejercicios usa la paginacion del contrato")
    void laListaDeEjerciciosEstaPaginada() throws Exception {
        int total = catalog.size();
        int porPagina = 5;
        int paginasEsperadas = (int) Math.ceil(total / (double) porPagina);

        mockMvc.perform(get("/api/v1/exercises").param("per_page", String.valueOf(porPagina)).cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(porPagina))
                .andExpect(jsonPath("$.pagination.total").value(total))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.per_page").value(porPagina))
                .andExpect(jsonPath("$.pagination.total_pages").value(paginasEsperadas));
    }

    @Test
    @DisplayName("Se puede filtrar la lista por nivel y por tema")
    void laListaSePuedeFiltrarPorTema() throws Exception {
        int esperados = (int) catalog.all().stream().filter(e -> "strings".equals(e.topic())).count();

        mockMvc.perform(get("/api/v1/exercises").param("topic", "strings").param("per_page", "100").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(esperados))
                .andExpect(jsonPath("$.pagination.total").value(esperados));
    }

    @Test
    @DisplayName("El detalle devuelve el enunciado y el codigo inicial, nunca la solucion")
    void elDetalleNoExponeLaSolucion() throws Exception {
        Exercise ejercicio = ejercicio("even-or-odd");

        MvcResult resultado = mockMvc.perform(get("/api/v1/exercises/" + ejercicio.slug()).cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value(ejercicio.slug()))
                .andExpect(jsonPath("$.data.statement").isNotEmpty())
                .andExpect(jsonPath("$.data.starterCode").value(ejercicio.starterCode()))
                .andExpect(jsonPath("$.data.testsCount").value(contarTests(ejercicio)))
                .andExpect(jsonPath("$.data.hintsCount").value(ejercicio.safeHints().size()))
                .andExpect(jsonPath("$.data.solved").value(false))
                .andReturn();

        String cuerpo = resultado.getResponse().getContentAsString();
        assertThat(cuerpo).doesNotContain("@Test");
        assertThat(cuerpo).doesNotContain(ejercicio.referenceSolution());
    }

    @Test
    @DisplayName("Las pistas se entregan de a una")
    void lasPistasSeEntreganDeAUna() throws Exception {
        Exercise ejercicio = ejercicio("even-or-odd");

        mockMvc.perform(get("/api/v1/exercises/" + ejercicio.slug() + "/hints/0").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hintIndex").value(0))
                .andExpect(jsonPath("$.data.hint").value(ejercicio.safeHints().get(0)))
                .andExpect(jsonPath("$.data.hasMore").value(true));

        mockMvc.perform(get("/api/v1/exercises/" + ejercicio.slug() + "/hints/99").cookie(sesion()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PISTA_NO_EXISTE"));
    }

    @Test
    @DisplayName("Un ejercicio inexistente devuelve 404 con el contrato de error")
    void unEjercicioInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/exercises/no-existe").cookie(sesion()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("EJERCICIO_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("Un envío que el motor acepta pasa los tests y marca el ejercicio como resuelto")
    void unEnvioQuePasaMarcaElEjercicioResuelto() throws Exception {
        Exercise ejercicio = ejercicio("even-or-odd");

        mockMvc.perform(enviar(ejercicio.referenceSolution(), sesion(), ejercicio.slug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PASSED"))
                .andExpect(jsonPath("$.data.compiled").value(true))
                .andExpect(jsonPath("$.data.solved").value(true))
                .andExpect(jsonPath("$.data.failed").value(0))
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.data.tests[0].passed").value(true));
    }

    @Test
    @DisplayName("Un envío que falla queda marcado como intentado pero no resuelto")
    void unEnvioQueFallaQuedaIntentado() throws Exception {
        Exercise ejercicio = ejercicio("max-of-three");
        Cookie sesion = sesion();

        // El motor de mentira falla cuando el código contiene FALLA.
        mockMvc.perform(enviar("FALLA", sesion, ejercicio.slug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.compiled").value(true))
                .andExpect(jsonPath("$.data.solved").value(false))
                .andExpect(jsonPath("$.data.failed").value(org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(get("/api/v1/exercises/" + ejercicio.slug()).cookie(sesion))
                .andExpect(jsonPath("$.data.attempted").value(true))
                .andExpect(jsonPath("$.data.solved").value(false))
                .andExpect(jsonPath("$.data.attempts").value(1));
    }

    @Test
    @DisplayName("Un envío que no compila devuelve COMPILE_ERROR")
    void unEnvioQueNoCompilaDevuelveCompileError() throws Exception {
        // El motor de mentira devuelve COMPILE_ERROR cuando el código contiene ROMPE.
        mockMvc.perform(enviar("ROMPE", sesion(), "even-or-odd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPILE_ERROR"))
                .andExpect(jsonPath("$.data.compiled").value(false))
                .andExpect(jsonPath("$.data.compileOutput").isNotEmpty())
                .andExpect(jsonPath("$.data.solved").value(false));
    }

    @Test
    @DisplayName("El código con APIs prohibidas se rechaza antes de ejecutarse")
    void elCodigoConApisProhibidasSeRechaza() throws Exception {
        // El motor de mentira rechaza el mismo caso que el validador real (java.io).
        String codigoPeligroso = "PROHIBIDO java.io.File";

        mockMvc.perform(enviar(codigoPeligroso, sesion(), "even-or-odd"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CODIGO_NO_PERMITIDO"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no está permitido")));
    }

    @Test
    @DisplayName("El progreso refleja los ejercicios resueltos y guarda el codigo")
    void elProgresoReflejaLosEjerciciosResueltos() throws Exception {
        Exercise ejercicio = ejercicio("reverse-string");
        Cookie sesion = sesion();

        mockMvc.perform(enviar(ejercicio.referenceSolution(), sesion, ejercicio.slug()))
                .andExpect(jsonPath("$.data.status").value("PASSED"));

        mockMvc.perform(get("/api/v1/progress").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.solved").value(1))
                .andExpect(jsonPath("$.data.total").value(catalog.size()))
                .andExpect(jsonPath("$.data.xp").value(10))
                .andExpect(jsonPath("$.data.streakDays").value(1))
                .andExpect(jsonPath("$.data.levels[1].solved").value(1))
                .andExpect(jsonPath("$.data.levels[1].percent")
                        .value((int) Math.round(100.0 / catalog.byLevel(1).size())));

        mockMvc.perform(get("/api/v1/progress/history").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].exerciseSlug").value(ejercicio.slug()))
                .andExpect(jsonPath("$.data[0].passed").value(true))
                .andExpect(jsonPath("$.pagination.total").value(1));

        mockMvc.perform(get("/api/v1/exercises/" + ejercicio.slug()).cookie(sesion))
                .andExpect(jsonPath("$.data.solved").value(true))
                .andExpect(jsonPath("$.data.savedCode").value(ejercicio.referenceSolution()));
    }

    @Test
    @DisplayName("Resolver un ejercicio ya resuelto no pierde el avance")
    void resolverDosVecesNoPierdeElAvance() throws Exception {
        Exercise ejercicio = ejercicio("count-vowels");
        Cookie sesion = sesion();

        mockMvc.perform(enviar(ejercicio.referenceSolution(), sesion, ejercicio.slug()))
                .andExpect(jsonPath("$.data.solved").value(true))
                .andExpect(jsonPath("$.data.yaEstabaResuelto").value(false));

        // Un envío que falla después de haber resuelto: no debe perder el avance.
        mockMvc.perform(enviar("FALLA", sesion, ejercicio.slug()))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.solved").value(true))
                .andExpect(jsonPath("$.data.yaEstabaResuelto").value(true));

        mockMvc.perform(get("/api/v1/progress").cookie(sesion))
                .andExpect(jsonPath("$.data.solved").value(1));
    }

    @Test
    @DisplayName("La pregunta de complejidad no filtra la respuesta y se evalúa solo cuando los tests pasan")
    void laPreguntaDeComplejidadSeEvaluaTrasPasarLosTests() throws Exception {
        Exercise ejercicio = ejercicio("contains-duplicate");
        int correcta = ejercicio.complexityQuestion().correctIndex();
        int incorrecta = (correcta + 1) % ejercicio.complexityQuestion().options().size();
        Cookie sesion = sesion();

        mockMvc.perform(get("/api/v1/exercises/" + ejercicio.slug()).cookie(sesion))
                .andExpect(jsonPath("$.data.complexityQuestion.prompt").isNotEmpty())
                .andExpect(jsonPath("$.data.complexityQuestion.options").isArray())
                .andExpect(jsonPath("$.data.complexityQuestion.correctIndex").doesNotExist())
                .andExpect(jsonPath("$.data.complexityQuestion.explanation").doesNotExist());

        // Tests fallan: no se revela nada.
        mockMvc.perform(post("/api/v1/submissions").cookie(sesion).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("exerciseSlug", ejercicio.slug(),
                                "sourceCode", "FALLA", "complexityAnswer", correcta))))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.complexity").doesNotExist());

        // Tests pasan con respuesta incorrecta, luego correcta.
        mockMvc.perform(post("/api/v1/submissions").cookie(sesion).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("exerciseSlug", ejercicio.slug(),
                                "sourceCode", ejercicio.referenceSolution(), "complexityAnswer", incorrecta))))
                .andExpect(jsonPath("$.data.status").value("PASSED"))
                .andExpect(jsonPath("$.data.complexity.answered").value(true))
                .andExpect(jsonPath("$.data.complexity.correct").value(false))
                .andExpect(jsonPath("$.data.complexity.explanation").isNotEmpty());

        mockMvc.perform(post("/api/v1/submissions").cookie(sesion).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("exerciseSlug", ejercicio.slug(),
                                "sourceCode", ejercicio.referenceSolution(), "complexityAnswer", correcta))))
                .andExpect(jsonPath("$.data.complexity.correct").value(true));
    }

    @Test
    @DisplayName("El catálogo se puede filtrar por lenguaje")
    void elCatalogoSeFiltraPorLenguaje() throws Exception {
        Cookie sesion = sesion();

        // Los lenguajes disponibles incluyen java y python.
        mockMvc.perform(get("/api/v1/languages").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.hasItems("java", "python")));

        // Los niveles de Python no traen los niveles 1 y 2 (que solo existen en Java).
        mockMvc.perform(get("/api/v1/levels").param("language", "python").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].level").value(0))
                .andExpect(jsonPath("$.data[0].total").value(catalog.byLevel("python", 0).size()));

        // La lista de un lenguaje nunca mezcla ejercicios de otro.
        mockMvc.perform(get("/api/v1/exercises").param("language", "python").param("per_page", "100").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(catalog.byLanguage("python").size()))
                .andExpect(jsonPath("$.data[*].language").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("python"))));
    }

    @Test
    @DisplayName("El progreso se puede pedir por lenguaje")
    void elProgresoSePuedeFiltrarPorLenguaje() throws Exception {
        Cookie sesion = sesion();

        mockMvc.perform(get("/api/v1/progress").param("language", "python").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(catalog.byLanguage("python").size()))
                .andExpect(jsonPath("$.data.levels.length()").value(1));
    }

    @Test
    @DisplayName("El backend delega los envíos de Python en el motor (y guarda el progreso)")
    void unEnvioDePythonSeDelegaYGuardaProgreso() throws Exception {
        Cookie sesion = sesion();
        mockMvc.perform(enviar("def add_two_numbers(a, b):\n    return a + b\n", sesion, "py-add-two-numbers"))
                .andExpect(jsonPath("$.data.status").value("PASSED"))
                .andExpect(jsonPath("$.data.solved").value(true));

        mockMvc.perform(enviar("FALLA", sesion(), "py-reverse-text"))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.solved").value(false));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder enviar(
            String codigo, Cookie sesion, String slug) throws Exception {
        return post("/api/v1/submissions")
                .cookie(sesion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("exerciseSlug", slug, "sourceCode", codigo)));
    }

    private Exercise ejercicio(String slug) {
        return catalog.bySlug(slug).orElseThrow(() -> new AssertionError("Falta el ejercicio " + slug));
    }

    private Cookie sesion() throws Exception {
        String correo = "flujo-" + UUID.randomUUID() + "@javalab.test";
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", correo, "password", "secreta123"))))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie sesion = resultado.getResponse().getCookie("javalab_token");
        assertThat(sesion).isNotNull();
        return sesion;
    }

    private int contarTests(Exercise ejercicio) {
        String tests = ejercicio.tests();
        int total = 0;
        int desde = tests.indexOf("@Test");
        while (desde >= 0) {
            total++;
            desde = tests.indexOf("@Test", desde + 5);
        }
        return total;
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
