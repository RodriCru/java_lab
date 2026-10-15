package com.lrch.javalab.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerPropertiesTest {

    private RunnerProperties conTimeout(long segundos) {
        return new RunnerProperties(false, null, segundos, null, null, null, 1, 60, null, null);
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

    @Test
    @DisplayName("El directorio de trabajo vacío deja el temporal del sistema (local)")
    void workDirVacioUsaElTemporal() {
        assertThat(conTimeout(10).workDirPath()).isNull();
    }

    @Test
    @DisplayName("Sin host-work-dir se asume que work-dir ya es válido en el host")
    void hostWorkDirCaeAWorkDir() {
        RunnerProperties props = new RunnerProperties(false, null, 10, null, null, null, 1, 60,
                "/work", null);
        assertThat(props.workDirPath()).isEqualTo(Path.of("/work"));
        assertThat(props.hostWorkDirOrDefault()).isEqualTo("/work");
    }

    @Test
    @DisplayName("Con host-work-dir distinto se usa el del host para montar en los runners")
    void hostWorkDirMandaCuandoEstaPuesto() {
        RunnerProperties props = new RunnerProperties(false, null, 10, null, null, null, 1, 60,
                "/work", "/srv/javalab/work");
        assertThat(props.hostWorkDirOrDefault()).isEqualTo("/srv/javalab/work");
    }
}
