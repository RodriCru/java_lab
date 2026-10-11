package com.lrch.javalab.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lrch.javalab.catalog.CatalogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/** Banco de preguntas en memoria, cargado de los YAML de {@code questions/} al arrancar. */
@Component
public class QuestionBank {

    private static final Logger log = LoggerFactory.getLogger(QuestionBank.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Archivo(List<Question> questions) {
    }

    private final Map<String, Question> byId;
    private final List<Question> ordered;

    public QuestionBank(CatalogProperties properties) {
        Path root = Paths.get(properties.questionsDir()).toAbsolutePath().normalize();
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        Map<String, Question> indice = new LinkedHashMap<>();
        List<String> problemas = new ArrayList<>();

        if (!Files.isDirectory(root)) {
            log.warn("No existe el directorio de preguntas: {}. El banco queda vacio.", root);
        } else {
            try (Stream<Path> archivos = Files.list(root)) {
                List<Path> yamls = archivos
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".yml"))
                        .sorted()
                        .toList();
                for (Path archivo : yamls) {
                    try {
                        Archivo contenido = yaml.readValue(Files.readString(archivo), Archivo.class);
                        for (Question q : contenido.questions() == null ? List.<Question>of() : contenido.questions()) {
                            List<String> errores = q.problemas();
                            if (!errores.isEmpty()) {
                                problemas.add(archivo.getFileName() + " / " + q.id() + ": " + String.join(", ", errores));
                            } else if (indice.putIfAbsent(q.id(), q) != null) {
                                problemas.add(q.id() + ": id duplicado, se ignora la repetida");
                            }
                        }
                    } catch (IOException ex) {
                        problemas.add(archivo.getFileName() + ": YAML invalido (" + ex.getMessage() + ")");
                    }
                }
            } catch (IOException ex) {
                throw new java.io.UncheckedIOException("No se pudo leer el directorio de preguntas: " + root, ex);
            }
        }

        this.byId = Map.copyOf(indice);
        this.ordered = List.copyOf(indice.values());
        problemas.forEach(p -> log.error("Pregunta con problemas -> {}", p));
        log.info("Banco de preguntas cargado desde {}: {} preguntas", root, ordered.size());
    }

    public List<Question> all() {
        return ordered;
    }

    public Optional<Question> byId(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public int size() {
        return ordered.size();
    }
}
