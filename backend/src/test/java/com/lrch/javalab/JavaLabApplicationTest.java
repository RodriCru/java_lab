package com.lrch.javalab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JavaLabApplicationTest {

    @Test
    @DisplayName("Crea el directorio que falta para el archivo SQLite")
    void creaElDirectorioDeLaBaseDeDatos(@TempDir Path temporal) {
        Path archivo = temporal.resolve("sub").resolve("datos").resolve("javalab.db");
        assertThat(archivo.getParent()).doesNotExist();

        conRuta(archivo, () -> {
            JavaLabApplication.prepararDirectorioDeLaBaseDeDatos();
            assertThat(archivo.getParent()).isDirectory();
            // El archivo lo crea SQLite, no este paso previo.
            assertThat(Files.exists(archivo)).isFalse();
        });
    }

    @Test
    @DisplayName("No falla si el directorio ya existe")
    void noFallaSiElDirectorioYaExiste(@TempDir Path temporal) {
        Path archivo = temporal.resolve("javalab.db");

        conRuta(archivo, () -> {
            JavaLabApplication.prepararDirectorioDeLaBaseDeDatos();
            JavaLabApplication.prepararDirectorioDeLaBaseDeDatos();
            assertThat(archivo.getParent()).isDirectory();
        });
    }

    @Test
    @DisplayName("Ignora las bases de datos en memoria")
    void ignoraLaBaseDeDatosEnMemoria() {
        conRutaDeTexto(":memory:", JavaLabApplication::prepararDirectorioDeLaBaseDeDatos);
    }

    private void conRuta(Path ruta, Runnable accion) {
        conRutaDeTexto(ruta.toString(), accion);
    }

    private void conRutaDeTexto(String ruta, Runnable accion) {
        String previa = System.getProperty("JAVALAB_DB_PATH");
        System.setProperty("JAVALAB_DB_PATH", ruta);
        try {
            accion.run();
        } finally {
            if (previa == null) {
                System.clearProperty("JAVALAB_DB_PATH");
            } else {
                System.setProperty("JAVALAB_DB_PATH", previa);
            }
        }
    }
}
