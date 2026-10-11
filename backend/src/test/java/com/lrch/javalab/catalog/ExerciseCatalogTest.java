package com.lrch.javalab.catalog;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Carga el catalogo real de ejercicios del repositorio.
 *
 * <p>Ademas de probar el cargador, valida que los archivos YAML escritos a mano cumplan el
 * esquema: es el primer filtro contra un ejercicio mal formado.
 */
class ExerciseCatalogTest {

    private static ExerciseCatalog catalogo;

    @BeforeAll
    static void cargar() {
        catalogo = new ExerciseCatalog(new CatalogProperties("../exercises", "../questions"));
    }

    @Test
    @DisplayName("Carga los niveles Fundamentos, Básico e Intermedio, cada uno con sus 50 ejercicios")
    void cargaLosTresNiveles() {
        assertThat(catalogo.levels()).containsExactly(0, 1, 2);
        assertThat(catalogo.byLevel(0)).hasSizeGreaterThanOrEqualTo(50);
        assertThat(catalogo.byLevel(1)).hasSizeGreaterThanOrEqualTo(50);
        assertThat(catalogo.byLevel(2)).hasSizeGreaterThanOrEqualTo(50);
        assertThat(catalogo.size()).isEqualTo(catalogo.byLevel(0).size() + catalogo.byLevel(1).size() + catalogo.byLevel(2).size());
    }

    @Test
    @DisplayName("Todo ejercicio de nivel 2 trae una pregunta de complejidad válida")
    void elNivel2TienePreguntaDeComplejidad() {
        assertThat(catalogo.byLevel(2))
                .allSatisfy(e -> assertThat(e.hasComplexityQuestion() && e.complexityQuestion().isValid())
                        .as(e.slug()).isTrue());
    }

    @Test
    @DisplayName("Carga los 50 ejercicios de la Fase 2")
    void cargaLosEjerciciosDeLaFase2() {
        // Muestra representativa de los 30 ejercicios agregados en la Fase 2.
        assertThat(catalogo.bySlug("is-prime")).isPresent();
        assertThat(catalogo.bySlug("binary-search")).isPresent();
        assertThat(catalogo.bySlug("towers-of-hanoi")).isPresent();
        assertThat(catalogo.bySlug("equals-books")).isPresent();
        assertThat(catalogo.bySlug("validate-parentheses")).isPresent();
    }

    @Test
    @DisplayName("Cada ejercicio tiene un slug unico y su metodo declarado")
    void cadaEjercicioTieneSlugYFirma() {
        assertThat(catalogo.all()).extracting(Exercise::slug).doesNotHaveDuplicates();

        for (Exercise ejercicio : catalogo.all()) {
            assertThat(ejercicio.slug()).as("slug").isNotBlank();
            assertThat(ejercicio.title()).as("titulo de %s", ejercicio.slug()).isNotBlank();
            assertThat(ejercicio.methodSignature()).as("firma de %s", ejercicio.slug())
                    .startsWith("public static");
            assertThat(ejercicio.className()).as("clase de %s", ejercicio.slug()).isEqualTo("Solution");
        }
    }

    @Test
    @DisplayName("El starter code y la solucion declaran la clase del ejercicio")
    void elStarterCodeDeclaraLaClaseEsperada() {
        for (Exercise ejercicio : catalogo.all()) {
            assertThat(ejercicio.starterCode()).as("starter de %s", ejercicio.slug())
                    .contains("public class " + ejercicio.className());
            assertThat(ejercicio.referenceSolution()).as("solucion de %s", ejercicio.slug())
                    .contains("public class " + ejercicio.className());
        }
    }

    @Test
    @DisplayName("Los tests y las pistas estan presentes en todos los ejercicios")
    void losTestsYPistasEstanPresentes() {
        for (Exercise ejercicio : catalogo.all()) {
            assertThat(ejercicio.tests()).as("tests de %s", ejercicio.slug()).contains("@Test");
            assertThat(contarTests(ejercicio)).as("cantidad de tests de %s", ejercicio.slug())
                    .isGreaterThanOrEqualTo(4);
            assertThat(ejercicio.safeHints()).as("pistas de %s", ejercicio.slug())
                    .hasSizeBetween(2, 3);
            assertThat(ejercicio.referenceSolution()).as("solucion de %s", ejercicio.slug())
                    .isNotEqualTo(ejercicio.starterCode());
        }
    }

    @Test
    @DisplayName("Los ejercicios se agrupan por tema dentro del nivel")
    void losEjerciciosSeAgrupanPorTema() {
        List<String> temas = catalogo.topicsOfLevel(1);

        // Temas del catálogo inicial (Fase 1) y los añadidos en la Fase 2.
        assertThat(temas).contains(
                "first-steps", "logic-math", "divisibility", "digits", "strings", "arrays",
                "matrices-patterns", "structures", "oop", "recursion", "sorting", "binary-search");

        // Todo tema usado por un ejercicio está descrito en topics.yml.
        assertThat(catalogo.all()).allSatisfy(ejercicio ->
                assertThat(catalogo.topic(ejercicio.topic())).as("tema de %s", ejercicio.slug()).isPresent());

        // Todo tema en uso trae su teoría (exercises/theory/<id>.md).
        assertThat(catalogo.all()).allSatisfy(ejercicio ->
                assertThat(catalogo.topic(ejercicio.topic()).orElseThrow().theory())
                        .as("teoría de %s", ejercicio.topic()).isNotBlank());

        // Cada ejercicio declara un tema y todos los temas listados tienen ejercicios.
        assertThat(catalogo.all()).allSatisfy(ejercicio ->
                assertThat(ejercicio.topic()).as("tema de %s", ejercicio.slug()).isNotBlank());
        assertThat(temas).allSatisfy(tema ->
                assertThat(catalogo.all().stream().filter(e -> tema.equals(e.topic()))).isNotEmpty());
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

    @Test
    @DisplayName("El validador de código acepta la referencia y el starter de cada ejercicio")
    void elValidadorAceptaTodoElCatalogo() {
        var validador = new com.lrch.javalab.runner.SourceValidator();
        assertThat(catalogo.all()).allSatisfy(e -> {
            validador.validar(e.referenceSolution(), e.className());
            validador.validar(e.starterCode(), e.className());
        });
    }
}
