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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Catalogo de ejercicios en memoria, cargado desde los archivos YAML del directorio configurado.
 * Se carga una sola vez al arrancar la aplicacion.
 *
 * <p>El directorio raiz contiene una carpeta por lenguaje ({@code java/}, {@code python/}...) y cada
 * una trae su propio {@code topics.yml}, su teoria ({@code theory/<id>.md}) y sus ejercicios. Los
 * temas se indexan con la clave {@code "<lenguaje>/<id>"} para que dos lenguajes puedan reutilizar
 * el mismo id (por ejemplo {@code java/strings} y {@code python/strings}).
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
        this.topics = new LinkedHashMap<>();
        this.bySlug = new LinkedHashMap<>();

        List<Path> lenguajes = directoriosDeLenguaje(root, problemas);
        if (lenguajes.isEmpty()) {
            problemas.add("No hay ninguna carpeta de lenguaje en " + root
                    + " (se esperaba, por ejemplo, 'java/')");
        }

        for (Path lenguajeDir : lenguajes) {
            String lenguaje = lenguajeDir.getFileName().toString().toLowerCase(Locale.ROOT);
            cargarTemas(lenguaje, lenguajeDir, problemas);

            try (Stream<Path> archivos = Files.walk(lenguajeDir)) {
                List<Path> yamls = archivos
                        .filter(Files::isRegularFile)
                        .filter(p -> {
                            String n = p.getFileName().toString().toLowerCase();
                            return (n.endsWith(".yml") || n.endsWith(".yaml")) && !n.equals(TOPICS_FILE);
                        })
                        .sorted()
                        .toList();

                for (Path archivo : yamls) {
                    String origen = root.relativize(archivo).toString();
                    try {
                        Exercise exercise = yamlMapper.readValue(Files.readString(archivo), Exercise.class);
                        // El lenguaje manda la carpeta; el YAML puede declararlo, pero si discrepa gana la carpeta.
                        // Solo se avisa si el YAML declara *otro* lenguaje (null es valido: se asume la carpeta).
                        if (exercise.language() != null && !exercise.language().isBlank()
                                && !lenguaje.equals(exercise.lenguaje())) {
                            problemas.add(origen + ": declara language='" + exercise.language()
                                    + "' pero vive en la carpeta '" + lenguaje + "'");
                        }
                        exercise = conLenguaje(exercise, lenguaje);
                        problemas.addAll(validar(exercise, origen));
                        if (!esBlanco(exercise.topic()) && !topics.containsKey(claveTema(lenguaje, exercise.topic()))) {
                            problemas.add(origen + ": el tema '" + exercise.topic()
                                    + "' no existe en " + lenguaje + "/" + TOPICS_FILE);
                        }
                        encontrados.add(exercise);
                    } catch (IOException ex) {
                        problemas.add(origen + ": YAML invalido (" + ex.getMessage() + ")");
                    }
                }
            } catch (IOException ex) {
                log.error("No se pudo leer la carpeta del lenguaje {}: {}", lenguaje, ex.getMessage());
            }
        }

        List<Exercise> finales = new ArrayList<>();
        for (Exercise exercise : encontrados) {
            if (bySlug.containsKey(exercise.slug())) {
                problemas.add(exercise.language() + "/" + exercise.slug()
                        + ": slug duplicado (los slugs son unicos en todo el laboratorio, "
                        + "aunque sean de lenguajes distintos), se ignora el archivo repetido");
                continue;
            }
            bySlug.put(exercise.slug(), exercise);
            finales.add(exercise);
        }
        finales.sort(Comparator.comparing(Exercise::language)
                .thenComparingInt(Exercise::level)
                .thenComparingInt(e -> ordenDelTema(e.language(), e.topic()))
                .thenComparingInt(e -> ordenDeDificultad(e.difficulty()))
                .thenComparing(Exercise::title, Comparator.nullsLast(Comparator.naturalOrder())));

        this.ordered = List.copyOf(finales);

        problemas.forEach(p -> log.error("Ejercicio con problemas -> {}", p));
        log.info("Catalogo cargado desde {}: {} ejercicios en {} lenguaje(s)",
                root, this.ordered.size(), lenguajes.size());
    }

    /** Copia el ejercicio fijando el lenguaje que manda la carpeta (no puede quedar nulo ni discrepar). */
    private static Exercise conLenguaje(Exercise e, String lenguaje) {
        return new Exercise(lenguaje, e.slug(), e.level(), e.topic(), e.title(), e.statement(),
                e.methodSignature(), e.className(), e.estimatedMinutes(), e.difficulty(), e.hints(),
                e.starterCode(), e.referenceSolution(), e.tests(), e.complexityQuestion());
    }

    /** Subcarpetas de primer nivel: una por lenguaje. */
    private List<Path> directoriosDeLenguaje(Path root, List<String> problemas) {
        try (Stream<Path> hijos = Files.list(root)) {
            return hijos.filter(Files::isDirectory)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .filter(Files::isDirectory)
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            problemas.add("No se pudo listar " + root + " (" + ex.getMessage() + ")");
            return List.of();
        }
    }

    /** Clave interna de un tema: el lenguaje forma parte de ella. */
    private static String claveTema(String lenguaje, String id) {
        return lenguaje + "/" + id;
    }

    /**
     * Prefijo obligatorio de los slugs de cada lenguaje, para que sean unicos en todo el laboratorio.
     * Java no lleva prefijo (es el lenguaje original); los demas usan una abreviatura legible en la URL.
     */
    private static String prefijoDeSlug(String lenguaje) {
        return switch (lenguaje) {
            case "python" -> "py-";
            case "typescript" -> "ts-";
            case "c" -> "c-";
            case "cpp" -> "cpp-";
            default -> lenguaje + "-";
        };
    }

    private void cargarTemas(String lenguaje, Path lenguajeDir, List<String> problemas) {
        Path archivo = lenguajeDir.resolve(TOPICS_FILE);
        if (!Files.isRegularFile(archivo)) {
            problemas.add("Falta " + lenguaje + "/" + TOPICS_FILE + " en " + lenguajeDir);
            return;
        }
        try {
            JsonNode lista = yamlMapper.readTree(Files.readString(archivo)).path("topics");
            for (JsonNode nodo : lista) {
                String id = nodo.path("id").asText("");
                String clave = claveTema(lenguaje, id);
                if (id.isBlank() || topics.containsKey(clave)) {
                    problemas.add(lenguaje + "/" + TOPICS_FILE + ": tema sin 'id' o repetido (" + id + ")");
                    continue;
                }
                String teoria = nodo.hasNonNull("theory") ? nodo.get("theory").asText()
                        : leerTeoria(lenguajeDir, id);
                topics.put(clave, new TopicInfo(id, nodo.path("name").asText(id),
                        nodo.path("description").asText(""), nodo.path("order").asInt(1000), teoria));
            }
        } catch (IOException ex) {
            problemas.add(lenguaje + "/" + TOPICS_FILE + ": YAML invalido (" + ex.getMessage() + ")");
        }
    }

    /** La teoría vive en {@code theory/<id>.md}, junto a topics.yml; si no existe, el tema no tiene teoría. */
    private String leerTeoria(Path lenguajeDir, String id) {
        Path archivo = lenguajeDir.resolve("theory").resolve(id + ".md");
        try {
            return Files.isRegularFile(archivo) ? Files.readString(archivo) : null;
        } catch (IOException ex) {
            log.warn("No se pudo leer la teoría de {}: {}", id, ex.getMessage());
            return null;
        }
    }

    private int ordenDelTema(String lenguaje, String topic) {
        TopicInfo tema = topics.get(claveTema(lenguaje, topic));
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
        } else if (!Exercise.LENGUAJE_POR_DEFECTO.equals(exercise.language())
                && !exercise.slug().startsWith(prefijoDeSlug(exercise.language()))) {
            // Los slugs son unicos en todo el laboratorio: los de otros lenguajes llevan prefijo
            // para no chocar con los de Java (por ejemplo, py-add-two-numbers frente a add-two-numbers).
            errores.add(origen + ": el slug '" + exercise.slug()
                    + "' debe empezar por '" + prefijoDeSlug(exercise.language())
                    + "' (los slugs son unicos entre lenguajes)");
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
        // className es un concepto de Java: los demas lenguajes no declaran clase.
        if (Exercise.LENGUAJE_POR_DEFECTO.equals(exercise.language()) && esBlanco(exercise.className())) {
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

    /** Lenguajes presentes en el catálogo, en orden alfabético ({@code java}, {@code python}...). */
    public List<String> languages() {
        return ordered.stream().map(Exercise::language).distinct().sorted().toList();
    }

    /** Ejercicios de un lenguaje concreto, en el orden del catálogo. */
    public List<Exercise> byLanguage(String lenguaje) {
        return ordered.stream().filter(e -> e.language().equals(lenguaje)).toList();
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
        return levels(null);
    }

    /** Niveles de un lenguaje; con {@code null}, los de todo el catálogo. */
    public List<Integer> levels(String lenguaje) {
        return (lenguaje == null ? ordered : byLanguage(lenguaje)).stream()
                .map(Exercise::level).distinct().sorted().toList();
    }

    public List<Exercise> byLevel(int level) {
        return byLevel(null, level);
    }

    /** Ejercicios de un nivel; con {@code lenguaje} nulo, los de todos los lenguajes. */
    public List<Exercise> byLevel(String lenguaje, int level) {
        return (lenguaje == null ? ordered : byLanguage(lenguaje)).stream()
                .filter(e -> e.level() == level).toList();
    }

    public List<String> topicsOfLevel(int level) {
        return topicsOfLevel(null, level);
    }

    public List<String> topicsOfLevel(String lenguaje, int level) {
        return byLevel(lenguaje, level).stream()
                .map(Exercise::topic)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .sorted(Comparator.comparingInt(t -> ordenDelTema(lenguaje, t)))
                .toList();
    }

    public Optional<TopicInfo> topic(String lenguaje, String id) {
        return Optional.ofNullable(topics.get(claveTema(lenguaje, id)));
    }

    public String topicName(String lenguaje, String id) {
        return topic(lenguaje, id).map(TopicInfo::name).orElse(id);
    }

    public int size() {
        return ordered.size();
    }
}
