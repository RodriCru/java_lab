package com.lrch.javalab.runnerservice;

import com.lrch.javalab.runner.ExecutionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ejecucion REAL del motor: compila y corre Java (javac + JUnit) y Python (pytest) de verdad.
 *
 * <p>Requiere que la maquina de tests tenga JDK y pytest, y el jar de JUnit en
 * {@code JAVALAB_JUNIT_JAR} (por defecto {@code /app/runner/...}).
 */
class RunnerEngineTest {

    private final RunnerEngine engine = new RunnerEngine();

    private static final String TESTS_JAVA = """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.*;

            class SolutionTest {
                @Test void sumaPositivos() { assertEquals(5, Solution.sumar(2, 3)); }
                @Test void sumaNegativos() { assertEquals(-2, Solution.sumar(-1, -1)); }
            }
            """;

    private static final String TESTS_PYTHON = """
            from solution import sumar

            def test_suma_positivos():
                assert sumar(2, 3) == 5

            def test_suma_negativos():
                assert sumar(-1, -1) == -2
            """;

    @Test
    @DisplayName("Java: la solución correcta pasa todos los tests")
    void javaPasa() {
        String codigo = """
                public class Solution {
                    public static int sumar(int a, int b) { return a + b; }
                }
                """;
        ExecutionResult r = engine.ejecutar(new RunRequest("java", "Solution", TESTS_JAVA, codigo));
        assertThat(r.status()).isEqualTo(ExecutionResult.Status.PASSED);
        assertThat(r.passedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Java: la solución incorrecta falla")
    void javaFalla() {
        String codigo = """
                public class Solution {
                    public static int sumar(int a, int b) { return a - b; }
                }
                """;
        ExecutionResult r = engine.ejecutar(new RunRequest("java", "Solution", TESTS_JAVA, codigo));
        assertThat(r.status()).isEqualTo(ExecutionResult.Status.FAILED);
    }

    @Test
    @DisplayName("Java: un error de compilación devuelve COMPILE_ERROR")
    void javaNoCompila() {
        String codigo = "public class Solution { public static int sumar(int a, int b) { return a + ; } }";
        ExecutionResult r = engine.ejecutar(new RunRequest("java", "Solution", TESTS_JAVA, codigo));
        assertThat(r.status()).isEqualTo(ExecutionResult.Status.COMPILE_ERROR);
        assertThat(r.compiled()).isFalse();
    }

    @Test
    @DisplayName("Python: la solución correcta pasa todos los tests")
    void pythonPasa() {
        String codigo = "def sumar(a, b):\n    return a + b\n";
        ExecutionResult r = engine.ejecutar(new RunRequest("python", "", TESTS_PYTHON, codigo));
        assertThat(r.status()).isEqualTo(ExecutionResult.Status.PASSED);
        assertThat(r.passedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Python: la solución incorrecta falla")
    void pythonFalla() {
        String codigo = "def sumar(a, b):\n    return a - b\n";
        ExecutionResult r = engine.ejecutar(new RunRequest("python", "", TESTS_PYTHON, codigo));
        assertThat(r.status()).isEqualTo(ExecutionResult.Status.FAILED);
    }

    @Test
    @DisplayName("Un lenguaje sin adaptador se rechaza con un mensaje claro")
    void lenguajeNoSoportado() {
        assertThat(assertThrows(com.lrch.javalab.runner.lang.RunnerException.class,
                () -> engine.ejecutar(new RunRequest("cobol", "", "tests", "codigo"))).getMessage())
                .contains("cobol");
    }
}
