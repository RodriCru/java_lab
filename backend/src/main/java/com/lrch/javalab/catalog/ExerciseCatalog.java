package com.lrch.javalab.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Catalogo de ejercicios en memoria, cargado desde los archivos YAML del directorio configurado.
 * Se carga una sola vez al arrancar la aplicacion.
 */
@Component
public class ExerciseCatalog {

    private static final Logger log = LoggerFactory.getLogger(ExerciseCatalog.class);
    private static final String DEFAULT_CLASS_NAME = "Solution";

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final String TOPICS_FILE = "topics.yml";

    private final Map<String, TopicInfo> topics;
    private final Map<String, Exercise> bySlug;
    private final List<Exercise> ordered;

    public ExerciseCatalog(CatalogProperties properties) {
        Path root = Paths.get(properties.exercisesDir()).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IllegalStateException(
                    "No existe el directorio de ejercicios: " + root
                            + ". Ajusta la propiedad javalab.catalog.exercises-dir.");
        }

        List<Exercise> encontrados = new ArrayList<>();
        List<String> problemas = new ArrayList<>();
        this.topics = cargarTemas(root, problemas);

        try (Stream<Path> archivos = Files.walk(root)) {
            List<Path> yamls = archivos
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase();
                        return (n.endsWith(".yml") || n.endsWith(".yaml")) && !n.equals(TOPICS_FILE);
                    })
                    .sorted()
                    .toList();

            for (Path archivo : yamls) {
                try {
                    Exercise exercise = yamlMapper.readValue(Files.readString(archivo), Exercise.class);
                    problemas.addAll(validar(exercise, root.relativize(archivo).toString()));
                    if (!esBlanco(exercise.topic()) && !topics.containsKey(exercise.topic())) {
                        problemas.add(root.relativize(archivo) + ": el tema '" + exercise.topic()
                                + "' no existe en " + TOPICS_FILE);
                    }
                    encontrados.add(exercise);
                } catch (IOException ex) {
                    problemas.add(root.relativize(archivo) + ": YAML invalido (" + ex.getMessage() + ")");
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer el directorio de ejercicios: " + root, ex);
        }

        Map<String, Exercise> indice = new LinkedHashMap<>();
        List<Exercise> finales = new ArrayList<>();
        for (Exercise exercise : encontrados) {
            if (indice.containsKey(exercise.slug())) {
                problemas.add(exercise.slug() + ": slug duplicado, se ignora el archivo repetido");
                continue;
            }
            indice.put(exercise.slug(), exercise);
            finales.add(exercise);
        }
        finales.sort(Comparator.comparingInt(Exercise::level)
                .thenComparingInt(e -> ordenDelTema(e.topic()))
                .thenComparingInt(e -> ordenDeDificultad(e.difficulty()))
                .thenComparing(Exercise::title, Comparator.nullsLast(Comparator.naturalOrder())));

        this.bySlug = Map.copyOf(indice);
        this.ordered = List.copyOf(finales);

        problemas.forEach(p -> log.error("Ejercicio con problemas -> {}", p));
        log.info("Catalogo cargado desde {}: {} ejercicios", root, this.ordered.size());
    }

    private Map<String, TopicInfo> cargarTemas(Path root, List<String> problemas) {
        Map<String, TopicInfo> resultado = new LinkedHashMap<>();
        Path archivo = root.resolve(TOPICS_FILE);
        if (!Files.isRegularFile(archivo)) {
            problemas.add("Falta " + TOPICS_FILE + " en " + root);
            return resultado;
        }
        try {
            JsonNode lista = yamlMapper.readTree(Files.readString(archivo)).path("topics");
            for (JsonNode nodo : lista) {
                String id = nodo.path("id").asText("");
                if (id.isBlank() || resultado.containsKey(id)) {
                    problemas.add(TOPICS_FILE + ": tema sin 'id' o repetido (" + id + ")");
                    continue;
                }
                String teoria = nodo.hasNonNull("theory") ? nodo.get("theory").asText() : leerTeoria(root, id);
                resultado.put(id, new TopicInfo(id, nodo.path("name").asText(id),
                        nodo.path("description").asText(""), nodo.path("order").asInt(1000), teoria));
            }
        } catch (IOException ex) {
            problemas.add(TOPICS_FILE + ": YAML invalido (" + ex.getMessage() + ")");
        }
        return resultado;
    }

    /** La teoría vive en {@code theory/<id>.md}, junto a topics.yml; si no existe, el tema no tiene teoría. */
    private String leerTeoria(Path root, String id) {
        Path archivo = root.resolve("theory").resolve(id + ".md");
        try {
            return Files.isRegularFile(archivo) ? Files.readString(archivo) : null;
        } catch (IOException ex) {
            log.warn("No se pudo leer la teoría de {}: {}", id, ex.getMessage());
            return null;
        }
    }

    private int ordenDelTema(String topic) {
        TopicInfo tema = topics.get(topic);
        return tema == null ? Integer.MAX_VALUE : tema.order();
    }

    private static int ordenDeDificultad(String dificultad) {
        if (dificultad == null) {
            return 3;
        }
        return switch (dificultad) {
            case "easy" -> 0;
            case "medium" -> 1;
            case "hard" -> 2;
            default -> 3;
        };
    }

    private List<String> validar(Exercise exercise, String origen) {
        List<String> errores = new ArrayList<>();
        if (esBlanco(exercise.slug())) {
            errores.add(origen + ": falta 'slug'");
        }
        if (exercise.level() < 0) {
            errores.add(origen + ": 'level' no puede ser negativo");
        }
        if (esBlanco(exercise.title())) {
            errores.add(origen + ": falta 'title'");
        }
        if (esBlanco(exercise.statement())) {
            errores.add(origen + ": falta 'statement'");
        }
        if (esBlanco(exercise.methodSignature())) {
            errores.add(origen + ": falta 'methodSignature'");
        }
        if (esBlanco(exercise.className())) {
            errores.add(origen + ": falta 'className' (usa '" + DEFAULT_CLASS_NAME + "')");
        }
        if (esBlanco(exercise.starterCode())) {
            errores.add(origen + ": falta 'starterCode'");
        }
        if (esBlanco(exercise.referenceSolution())) {
            errores.add(origen + ": falta 'referenceSolution'");
        }
        if (esBlanco(exercise.tests())) {
            errores.add(origen + ": falta 'tests'");
        }
        if (exercise.level() >= 2 && !exercise.hasComplexityQuestion()) {
            errores.add(origen + ": los ejercicios de nivel 2 o más requieren 'complexityQuestion'");
        }
        if (exercise.hasComplexityQuestion() && !exercise.complexityQuestion().isValid()) {
            errores.add(origen + ": 'complexityQuestion' inválida (prompt, 2+ options, correctIndex en rango y explanation)");
        }
        return errores;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    public List<Exercise> all() {
        return ordered;
    }

    public Optional<Exercise> bySlug(String slug) {
        return Optional.ofNullable(bySlug.get(slug));
    }

    /** El ejercicio que sigue en el orden del catálogo (cruza de nivel); vacío si es el último. */
    public Optional<Exercise> next(String slug) {
        for (int i = 0; i < ordered.size() - 1; i++) {
            if (ordered.get(i).slug().equals(slug)) {
                return Optional.of(ordered.get(i + 1));
            }
        }
        return Optional.empty();
    }

    public List<Integer> levels() {
        return ordered.stream().map(Exercise::level).distinct().sorted().toList();
    }

    public List<Exercise> byLevel(int level) {
        return ordered.stream().filter(e -> e.level() == level).toList();
    }

    public List<String> topicsOfLevel(int level) {
        return byLevel(level).stream()
                .map(Exercise::topic)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .sorted(Comparator.comparingInt(this::ordenDelTema))
                .toList();
    }

    public Optional<TopicInfo> topic(String id) {
        return Optional.ofNullable(topics.get(id));
    }

    public String topicName(String id) {
        return topic(id).map(TopicInfo::name).orElse(id);
    }

    public int size() {
        return ordered.size();
    }
}
