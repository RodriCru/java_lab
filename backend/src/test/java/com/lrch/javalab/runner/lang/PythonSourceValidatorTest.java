package com.lrch.javalab.runner.lang;

import com.lrch.javalab.common.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PythonSourceValidatorTest {

    private final PythonSourceValidator validador = new PythonSourceValidator();

    private static final String CODIGO_VALIDO = """
            import math

            def area_circulo(radio):
                return math.pi * radio ** 2
            """;

    @Test
    @DisplayName("Acepta código que solo usa módulos permitidos")
    void aceptaCodigoValido() {
        assertThatCode(() -> validador.validar(CODIGO_VALIDO)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Rechaza código vacío")
    void rechazaVacio() {
        assertThatThrownBy(() -> validador.validar("   "))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("CODIGO_VACIO");
    }

    @Test
    @DisplayName("Rechaza imports no permitidos")
    void rechazaImportsProhibidos() {
        String codigo = """
                import os

                def leer():
                    return os.listdir(".")
                """;
        assertThatThrownBy(() -> validador.validar(codigo))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("CODIGO_NO_PERMITIDO");
    }

    @Test
    @DisplayName("Rechaza subprocess y eval")
    void rechazaSubprocessYExec() {
        assertThatThrownBy(() -> validador.validar("import subprocess\n"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validador.validar("def f(x):\n    return eval(x)\n"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("No rechaza un texto que solo menciona un token prohibido dentro de una cadena")
    void ignoraLosTokensDentroDeCadenas() {
        String codigo = """
                def explicar():
                    return "usa open( para leer un archivo"
                """;
        assertThatCode(() -> validador.validar(codigo)).doesNotThrowAnyException();
    }
}
