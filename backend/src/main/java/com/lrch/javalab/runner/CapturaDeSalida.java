package com.lrch.javalab.runner;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hace que cada test informe de lo que el codigo del alumno imprimio en consola.
 *
 * <p>JUnit no captura la salida estandar por test, asi que se inyecta una extension junto a
 * los tests: redirige {@code System.out} antes de cada uno y escribe lo capturado, codificado, en la consola real
 * (una linea marcada con {@link #MARCA}); el backend la lee y la quita de la salida visible. Si el test ya redirige la salida por su cuenta, no hay conflicto:
 * restaura la que encontro, que es la de la extension.
 */
public final class CapturaDeSalida {

    /** Prefijo de las lineas que la extension escribe en la consola real: {@code MARCA nombre|base64}. */
    static final String MARCA = "\u0001JAVALAB|";
    public static final String CLASE = "JavaLabCapture";
    private static final int MAX_CHARS = 1_000;

    private static final Pattern DECLARACION_TESTS =
            Pattern.compile("(?m)^([ \\t]*)((?:public\\s+)?class\\s+" + AbstractJUnitRunner.TEST_CLASS + "\\b)");

    public static final String FUENTE = """
            import org.junit.jupiter.api.extension.AfterEachCallback;
            import org.junit.jupiter.api.extension.BeforeEachCallback;
            import org.junit.jupiter.api.extension.ExtensionContext;

            import java.io.ByteArrayOutputStream;
            import java.io.PrintStream;
            import java.nio.charset.StandardCharsets;

            public class JavaLabCapture implements BeforeEachCallback, AfterEachCallback {
                private PrintStream original;
                private ByteArrayOutputStream buffer;

                @Override
                public void beforeEach(ExtensionContext context) {
                    original = System.out;
                    buffer = new ByteArrayOutputStream();
                    System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
                }

                @Override
                public void afterEach(ExtensionContext context) {
                    System.out.flush();
                    System.setOut(original);
                    String texto = buffer.toString(StandardCharsets.UTF_8);
                    if (!texto.isEmpty()) {
                        if (texto.length() > %d) {
                            texto = texto.substring(0, %d) + "...";
                        }
                        String nombre = context.getRequiredTestMethod().getName();
                        String codificado = java.util.Base64.getEncoder()
                                .encodeToString(texto.getBytes(StandardCharsets.UTF_8));
                        original.println("%s" + nombre + "|" + codificado);
                    }
                }
            }
            """.formatted(MAX_CHARS, MAX_CHARS, MARCA);

    private CapturaDeSalida() {
    }

    /** Salidas por test y consola sin las lineas de marcado. */
    public record Extraido(Map<String, String> salidas, String consola) {
    }

    /** Separa de la consola las lineas que escribio la extension. */
    public static Extraido extraer(String consola) {
        Map<String, String> salidas = new HashMap<>();
        if (consola == null || !consola.contains(MARCA)) {
            return new Extraido(salidas, consola);
        }
        StringBuilder limpia = new StringBuilder();
        for (String linea : consola.split("\n", -1)) {
            if (linea.startsWith(MARCA)) {
                int barra = linea.indexOf('|', MARCA.length());
                if (barra > 0) {
                    try {
                        salidas.put(linea.substring(MARCA.length(), barra), new String(
                                Base64.getDecoder().decode(linea.substring(barra + 1).strip()), StandardCharsets.UTF_8));
                    } catch (IllegalArgumentException ignorada) {
                        // linea corrupta: se descarta
                    }
                }
            } else {
                limpia.append(linea).append('\n');
            }
        }
        return new Extraido(salidas, limpia.toString());
    }

    /** Anade {@code @ExtendWith} a la clase de tests. */
    public static String conExtension(String testCode) {
        Matcher m = DECLARACION_TESTS.matcher(testCode);
        return m.replaceFirst("$1@org.junit.jupiter.api.extension.ExtendWith(" + CLASE + ".class)\n$1$2");
    }
}
