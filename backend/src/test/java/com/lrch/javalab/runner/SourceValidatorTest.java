package com.lrch.javalab.runner;

import com.lrch.javalab.common.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourceValidatorTest {

    private final SourceValidator validador = new SourceValidator();

    private static final String CODIGO_VALIDO = """
            import java.util.Arrays;

            public class Solution {
                public static int sumar(int a, int b) {
                    return a + b;
                }
            }
            """;

    @Test
    @DisplayName("Acepta una solucion que solo usa java.util")
    void aceptaCodigoValido() {
        assertThatCode(() -> validador.validar(CODIGO_VALIDO, "Solution")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Acepta RuntimeException pero sigue rechazando Runtime")
    void aceptaRuntimeExceptionPeroNoRuntime() {
        String conExcepcion = """
                public class Solution {
                    static class Fallo extends RuntimeException { }
                    public static int f() { throw new Fallo(); }
                }
                """;
        assertThatCode(() -> validador.validar(conExcepcion, "Solution")).doesNotThrowAnyException();

        String conRuntime = """
                public class Solution {
                    public static int f() { return Runtime.getRuntime().availableProcessors(); }
                }
                """;
        assertThatThrownBy(() -> validador.validar(conRuntime, "Solution")).isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Permite Scanner sobre un String pero no leer de System.in")
    void permiteScannerSobreStringPeroNoSystemIn() {
        String sobreString = """
                import java.util.Scanner;
                public class Solution {
                    public static int f(String s) { return new Scanner(s).nextInt(); }
                }
                """;
        assertThatCode(() -> validador.validar(sobreString, "Solution")).doesNotThrowAnyException();

        String sobreEntrada = """
                import java.util.Scanner;
                public class Solution {
                    public static int f() { return new Scanner(System.in).nextInt(); }
                }
                """;
        assertThatThrownBy(() -> validador.validar(sobreEntrada, "Solution")).isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Rechaza imports fuera de java.util y java.math")
    void rechazaImportsNoPermitidos() {
        String codigo = """
                import java.io.File;

                public class Solution {
                    public static int sumar(int a, int b) { return a + b; }
                }
                """;

        assertThatThrownBy(() -> validador.validar(codigo, "Solution"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo("CODIGO_NO_PERMITIDO");
    }

    @Test
    @DisplayName("Rechaza el uso de APIs peligrosas aunque no haya import")
    void rechazaApisPeligrosas() {
        String[] peligrosos = {
                "System.exit(0);",
                "new ProcessBuilder(\"rm\", \"-rf\", \"/\");",
                "Runtime.getRuntime();",
                "new Thread(() -> {});"
        };

        for (String fragmento : peligrosos) {
            String codigo = "public class Solution {\n"
                    + "    public static int sumar(int a, int b) {\n"
                    + "        " + fragmento + "\n"
                    + "        return a + b;\n"
                    + "    }\n"
                    + "}";

            assertThatThrownBy(() -> validador.validar(codigo, "Solution"))
                    .as("deberia rechazar: %s", fragmento)
                    .isInstanceOf(ApiException.class);
        }
    }

    @Test
    @DisplayName("Rechaza un codigo que no declara la clase esperada")
    void rechazaClaseEquivocada() {
        String codigo = """
                public class OtraCosa {
                    public static int sumar(int a, int b) { return a + b; }
                }
                """;

        assertThatThrownBy(() -> validador.validar(codigo, "Solution"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Solution");
    }

    @Test
    @DisplayName("Rechaza el codigo vacio o demasiado largo")
    void rechazaCodigoVacio() {
        assertThatThrownBy(() -> validador.validar("   ", "Solution"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo("CODIGO_VACIO");

        assertThatThrownBy(() -> validador.validar("public class Solution { " + "x".repeat(20_001), "Solution"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo("CODIGO_DEMASIADO_LARGO");
    }

    @Test
    @DisplayName("El mensaje de error explica que se rechazo")
    void elMensajeExplicaElMotivo() {
        String codigo = """
                public class Solution {
                    public static int sumar(int a, int b) {
                        System.exit(1);
                        return a + b;
                    }
                }
                """;

        assertThatThrownBy(() -> validador.validar(codigo, "Solution"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("System.exit");
    }

    @Test
    @DisplayName("Acepta un codigo largo pero dentro del limite")
    void aceptaCodigoLargoDentroDelLimite() {
        String relleno = "// relleno\n".repeat(1_000);
        String codigo = "public class Solution {\n" + relleno
                + "    public static int sumar(int a, int b) { return a + b; }\n}";

        assertThat(codigo.length()).isLessThan(20_000);
        assertThatCode(() -> validador.validar(codigo, "Solution")).doesNotThrowAnyException();
    }
}
