package com.lrch.javalab.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerPropertiesTest {

    private RunnerProperties conTimeout(long segundos) {
        return new RunnerProperties("http://runner:8081", segundos, 1, 60);
    }

    @Test
    @DisplayName("Un timeout absurdo se recorta al máximo: no debe bloquear el runner durante minutos")
    void recortaTimeoutDemasiadoAlto() {
        assertThat(conTimeout(300).timeoutSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("Un timeout razonable se respeta tal cual")
    void respetaTimeoutValido() {
        assertThat(conTimeout(10).timeoutSeconds()).isEqualTo(10);
        assertThat(conTimeout(60).timeoutSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("Un timeout no positivo cae al valor por defecto")
    void timeoutNoPositivoUsaElDefecto() {
        assertThat(conTimeout(0).timeoutSeconds()).isEqualTo(10);
        assertThat(conTimeout(-5).timeoutSeconds()).isEqualTo(10);
    }

    @Test
    @DisplayName("Sin URL del servicio se usa el valor por defecto de local")
    void urlPorDefecto() {
        assertThat(new RunnerProperties(null, 10, 1, 60).serviceUrl())
                .isEqualTo("http://localhost:8081");
    }
}
