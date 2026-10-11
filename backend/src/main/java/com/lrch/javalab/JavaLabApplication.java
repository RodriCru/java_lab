package com.lrch.javalab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JavaLabApplication {

    /** Mismo valor por defecto que spring.datasource.url en application.yml. */
    private static final String RUTA_POR_DEFECTO = "./data/javalab.db";

    public static void main(String[] args) {
        prepararDirectorioDeLaBaseDeDatos();
        SpringApplication.run(JavaLabApplication.class, args);
    }

    /**
     * SQLite no crea directorios: si falta la carpeta que contiene el archivo, la conexion falla
     * al arrancar. Se crea aqui, antes de que Spring levante el DataSource.
     */
    static void prepararDirectorioDeLaBaseDeDatos() {
        String ruta = rutaDeLaBaseDeDatos();
        if (ruta.startsWith(":")) {
            // Bases de datos en memoria (por ejemplo ":memory:") no tienen archivo.
            return;
        }

        Path directorio = Paths.get(ruta).toAbsolutePath().normalize().getParent();
        if (directorio == null || Files.isDirectory(directorio)) {
            return;
        }

        try {
            Files.createDirectories(directorio);
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "No se pudo crear el directorio de la base de datos: " + directorio, ex);
        }
    }

    private static String rutaDeLaBaseDeDatos() {
        String desdePropiedad = System.getProperty("JAVALAB_DB_PATH");
        if (desdePropiedad != null && !desdePropiedad.isBlank()) {
            return desdePropiedad;
        }
        String desdeEntorno = System.getenv("JAVALAB_DB_PATH");
        if (desdeEntorno != null && !desdeEntorno.isBlank()) {
            return desdeEntorno;
        }
        return RUTA_POR_DEFECTO;
    }
}
