package com.lrch.javalab.catalog;

import com.lrch.javalab.common.ApiException;
import com.lrch.javalab.common.PageResult;
import com.lrch.javalab.progress.ProgressService;
import com.lrch.javalab.progress.UserSolution;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Combina el catalogo (estatico) con el avance del usuario para armar las respuestas de la API. */
@Service
public class CatalogService {

    /** Estados por los que se puede filtrar la lista de ejercicios. */
    public enum EstadoFiltro {
        TODOS, RESUELTOS, PENDIENTES, FALLADOS;

        static EstadoFiltro desde(String valor) {
            if (valor == null || valor.isBlank()) {
                return TODOS;
            }
            return switch (valor.trim().toLowerCase(Locale.ROOT)) {
                case "solved", "resueltos" -> RESUELTOS;
                case "pending", "pendientes" -> PENDIENTES;
                case "failed", "fallados" -> FALLADOS;
                default -> TODOS;
            };
        }
    }

    private final ExerciseCatalog catalog;
    private final ProgressService progressService;

    public CatalogService(ExerciseCatalog catalog, ProgressService progressService) {
        this.catalog = catalog;
        this.progressService = progressService;
    }

    public List<LevelResponse> niveles(Long userId) {
        return niveles(userId, null);
    }

    /** Niveles de un lenguaje; con {@code lenguaje} nulo, los de todo el catálogo. */
    public List<LevelResponse> niveles(Long userId, String lenguaje) {
        Map<String, UserSolution> soluciones = progressService.solucionesPorSlug(userId);
        return catalog.levels(lenguaje).stream()
                .map(nivel -> {
                    List<Exercise> delNivel = catalog.byLevel(lenguaje, nivel);
                    int resueltos = (int) delNivel.stream()
                            .filter(e -> esResuelto(soluciones.get(e.slug())))
                            .count();
                    int total = delNivel.size();
                    int porcentaje = total == 0 ? 0 : (int) Math.round(resueltos * 100.0 / total);
                    List<TopicSection> secciones = catalog.topicsOfLevel(lenguaje, nivel).stream()
                            .map(id -> {
                                List<Exercise> deTema = delNivel.stream()
                                        .filter(e -> id.equals(e.topic()))
                                        .toList();
                                int hechos = (int) deTema.stream()
                                        .filter(e -> esResuelto(soluciones.get(e.slug())))
                                        .count();
                                TopicInfo tema = catalog.topic(lenguaje, id).orElse(null);
                                return new TopicSection(id, tema == null ? id : tema.name(),
                                        tema == null ? "" : tema.description(), deTema.size(), hechos);
                            })
                            .toList();
                    return new LevelResponse(nivel, nombreDeNivel(nivel), total, resueltos, porcentaje,
                            catalog.topicsOfLevel(lenguaje, nivel), secciones);
                })
                .toList();
    }

    public PageResult<ExerciseSummary> listar(Long userId, Integer level, String topic, String estado,
                                              String busqueda, int page, int perPage) {
        return listar(userId, null, level, topic, estado, busqueda, page, perPage);
    }

    public PageResult<ExerciseSummary> listar(Long userId, String lenguaje, Integer level, String topic,
                                              String estado, String busqueda, int page, int perPage) {
        Map<String, UserSolution> soluciones = progressService.solucionesPorSlug(userId);
        Set<String> intentados = progressService.slugsIntentados(userId);
        EstadoFiltro filtro = EstadoFiltro.desde(estado);
        String texto = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);

        List<ExerciseSummary> filtrados = catalog.all().stream()
                .filter(e -> lenguaje == null || e.language().equals(lenguaje))
                .filter(e -> level == null || e.level() == level)
                .filter(e -> topic == null || topic.isBlank() || topic.equalsIgnoreCase(e.topic()))
                .filter(e -> texto.isEmpty()
                        || e.title().toLowerCase(Locale.ROOT).contains(texto)
                        || e.slug().toLowerCase(Locale.ROOT).contains(texto))
                .filter(e -> coincideConEstado(filtro, e.slug(), soluciones, intentados))
                .map(e -> resumen(e, soluciones.get(e.slug()), intentados.contains(e.slug())))
                .toList();

        long total = filtrados.size();
        int desde = Math.min((page - 1) * perPage, filtrados.size());
        int hasta = Math.min(desde + perPage, filtrados.size());
        return new PageResult<>(filtrados.subList(desde, hasta), total, page, perPage);
    }

    public ExerciseDetail detalle(Long userId, String slug) {
        Exercise ejercicio = buscar(slug);
        UserSolution solucion = progressService.solucionesPorSlug(userId).get(slug);
        Set<String> intentados = progressService.slugsIntentados(userId);
        List<String> pistas = ejercicio.safeHints();

        return new ExerciseDetail(
                ejercicio.slug(),
                ejercicio.language(),
                ejercicio.level(),
                ejercicio.topic(),
                catalog.topicName(ejercicio.language(), ejercicio.topic()),
                catalog.topic(ejercicio.language(), ejercicio.topic()).map(TopicInfo::description).orElse(""),
                catalog.topic(ejercicio.language(), ejercicio.topic()).map(TopicInfo::theory).orElse(null),
                ejercicio.title(),
                ejercicio.statement(),
                ejercicio.methodSignature(),
                ejercicio.className(),
                ejercicio.starterCode(),
                ejercicio.difficulty(),
                ejercicio.estimatedMinutes(),
                ejercicio.hasComplexityQuestion() ? ejercicio.complexityQuestion().view() : null,
                contarTests(ejercicio),
                pistas.size(),
                esResuelto(solucion),
                intentados.contains(slug),
                solucion == null ? 0 : solucion.getAttempts(),
                solucion == null ? null : solucion.getCode(),
                catalog.next(slug).map(Exercise::slug).orElse(null),
                catalog.next(slug).map(Exercise::title).orElse(null));
    }

    public HintResponse pista(String slug, int indice) {
        Exercise ejercicio = buscar(slug);
        List<String> pistas = ejercicio.safeHints();
        if (pistas.isEmpty()) {
            throw ApiException.notFound("SIN_PISTAS", "Este ejercicio no tiene pistas.");
        }
        if (indice < 0 || indice >= pistas.size()) {
            throw ApiException.notFound("PISTA_NO_EXISTE",
                    "La pista " + indice + " no existe. Este ejercicio tiene " + pistas.size() + " pistas.");
        }        return new HintResponse(slug, indice, pistas.get(indice), pistas.size(), indice < pistas.size() - 1);
    }

    public Exercise buscar(String slug) {
        return catalog.bySlug(slug)
                .orElseThrow(() -> ApiException.notFound("EJERCICIO_NO_ENCONTRADO",
                        "No existe el ejercicio «" + slug + "»."));
    }

    /** Lenguajes presentes en el catálogo, en orden alfabético. */
    public List<String> lenguajes() {
        return catalog.languages();
    }

    private ExerciseSummary resumen(Exercise ejercicio, UserSolution solucion, boolean intentado) {
        return new ExerciseSummary(
                ejercicio.slug(),
                ejercicio.language(),
                ejercicio.level(),
                ejercicio.topic(),
                catalog.topicName(ejercicio.language(), ejercicio.topic()),
                ejercicio.title(),
                ejercicio.difficulty(),
                ejercicio.estimatedMinutes(),
                contarTests(ejercicio),
                esResuelto(solucion),
                intentado);
    }

    private boolean coincideConEstado(EstadoFiltro filtro, String slug,
                                      Map<String, UserSolution> soluciones, Set<String> intentados) {
        boolean resuelto = esResuelto(soluciones.get(slug));
        return switch (filtro) {
            case TODOS -> true;
            case RESUELTOS -> resuelto;
            case PENDIENTES -> !intentados.contains(slug);
            case FALLADOS -> intentados.contains(slug) && !resuelto;
        };
    }

    private boolean esResuelto(UserSolution solucion) {
        return solucion != null && solucion.isSolved();
    }

    /** Cuantos metodos de prueba tiene el ejercicio, segun el lenguaje. */
    private int contarTests(Exercise ejercicio) {
        String tests = ejercicio.tests();
        if (tests == null) {
            return 0;
        }
        return switch (ejercicio.language()) {
            case "python" -> contarOcurrencias(tests, "def test_");
            default -> contarOcurrencias(tests, "@Test");
        };
    }

    private int contarOcurrencias(String texto, String aguja) {
        int total = 0;
        int desde = texto.indexOf(aguja);
        while (desde >= 0) {
            total++;
            desde = texto.indexOf(aguja, desde + aguja.length());
        }
        return total;
    }

    private String nombreDeNivel(int nivel) {
        return switch (nivel) {
            case 0 -> "Fundamentos";
            case 1 -> "Básico";
            case 2 -> "Intermedio";
            default -> "Nivel " + nivel;
        };
    }
}
