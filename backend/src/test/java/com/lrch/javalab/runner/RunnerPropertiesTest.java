package com.lrch.javalab.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerPropertiesTest {

    private RunnerProperties conTimeout(long segundos) {
        return new RunnerProperties(false, null, segundos, null, null, null, 1, 60);
    }

    @Test
    @DisplayName("Un timeout absurdo se recorta al máximo: no debe bloquear el runner durante minutos")
    void recortaTimeoutDemasiadoAlto() {
        // Un ciclo infinito en Coolify se colgó 300 segundos: el navegador corta antes y el
        // usuario solo ve un error de red, mientras el runner queda ocupado todo ese tiempo.
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
}
