package com.lrch.javalab.runnerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Servicio que ejecuta el codigo del alumno. Su unico trabajo es compilar y correr los tests de un
 * ejercicio y devolver el resultado; no sabe nada del catalogo, de la autenticacion ni de la base de
 * datos (eso vive en el backend).
 */
@SpringBootApplication
public class RunnerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunnerServiceApplication.class, args);
    }
}
