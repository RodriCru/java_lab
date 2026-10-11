package com.lrch.javalab.progress;

import com.lrch.javalab.catalog.Exercise;
import com.lrch.javalab.catalog.ExerciseCatalog;
import com.lrch.javalab.runner.ExecutionResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    /** Puntos que otorga cada ejercicio resuelto. */
    public static final int XP_POR_EJERCICIO = 10;

    private final AttemptRepository attemptRepository;
    private final UserSolutionRepository solutionRepository;
    private final ExerciseCatalog catalog;

    public ProgressService(AttemptRepository attemptRepository,
                           UserSolutionRepository solutionRepository,
                           ExerciseCatalog catalog) {
        this.attemptRepository = attemptRepository;
        this.solutionRepository = solutionRepository;
        this.catalog = catalog;
    }

    @Transactional
    public boolean registrarIntento(Long userId, Exercise ejercicio, String codigo, ExecutionResult resultado) {
        attemptRepository.save(new Attempt(
                userId,
                ejercicio.slug(),
                resultado.allPassed(),
                (int) resultado.passedCount(),
                resultado.tests().size(),
                resultado.status().name()));

        UserSolution solucion = solutionRepository.findByUserIdAndExerciseSlug(userId, ejercicio.slug())
                .orElseGet(() -> new UserSolution(userId, ejercicio.slug()));
        solucion.registrarEnvio(codigo, resultado.allPassed());
        solutionRepository.save(solucion);
        return solucion.isSolved();
    }

    @Transactional(readOnly = true)
    public ProgressSummary resumen(Long userId) {
        Map<String, UserSolution> soluciones = solucionesPorSlug(userId);

        List<LevelProgress> niveles = catalog.levels().stream()
                .map(nivel -> {
                    List<Exercise> delNivel = catalog.byLevel(nivel);
                    int resueltos = (int) delNivel.stream()
                            .filter(e -> esResuelto(soluciones.get(e.slug())))
                            .count();
                    return new LevelProgress(nivel, delNivel.size(), resueltos);
                })
                .toList();

        int resueltosTotales = (int) soluciones.values().stream().filter(UserSolution::isSolved).count();

        return new ProgressSummary(
                (long) resueltosTotales * XP_POR_EJERCICIO,
                resueltosTotales,
                catalog.size(),
                calcularRacha(userId),
                (int) attemptRepository.countByUserId(userId),
                niveles);
    }

    @Transactional(readOnly = true)
    public Map<String, UserSolution> solucionesPorSlug(Long userId) {
        return solutionRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserSolution::getExerciseSlug, Function.identity(), (a, b) -> a));
    }

    @Transactional(readOnly = true)
    public Set<String> slugsIntentados(Long userId) {
        return new HashSet<>(attemptRepository.findSlugsIntentados(userId));
    }

    @Transactional(readOnly = true)
    public Set<String> slugsResueltos(Long userId) {
        return new HashSet<>(attemptRepository.findSlugsResueltos(userId));
    }

    @Transactional(readOnly = true)
    public List<AttemptResponse> historial(Long userId, int page, int porPagina) {
        return attemptRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, porPagina)).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public long totalIntentos(Long userId) {
        return attemptRepository.countByUserId(userId);
    }

    private AttemptResponse aRespuesta(Attempt intento) {
        Exercise ejercicio = catalog.bySlug(intento.getExerciseSlug()).orElse(null);
        return new AttemptResponse(
                intento.getExerciseSlug(),
                ejercicio == null ? intento.getExerciseSlug() : ejercicio.title(),
                ejercicio == null ? 0 : ejercicio.level(),
                intento.isPassed(),
                intento.getStatus(),
                intento.getTestsPassed(),
                intento.getTestsTotal(),
                Instant.ofEpochMilli(intento.getCreatedAt()));
    }

    private boolean esResuelto(UserSolution solucion) {
        return solucion != null && solucion.isSolved();
    }

    /** Dias consecutivos resolviendo al menos un ejercicio, contando hasta hoy o hasta ayer. */
    private int calcularRacha(Long userId) {
        ZoneId zona = ZoneId.systemDefault();
        Set<LocalDate> dias = attemptRepository.findInstantesResueltos(userId).stream()
                .map(instante -> Instant.ofEpochMilli(instante).atZone(zona).toLocalDate())
                .collect(Collectors.toSet());

        if (dias.isEmpty()) {
            return 0;
        }

        LocalDate hoy = LocalDate.now(zona);
        LocalDate cursor = dias.contains(hoy) ? hoy : hoy.minusDays(1);
        if (!dias.contains(cursor)) {
            return 0;
        }

        int racha = 0;
        while (dias.contains(cursor)) {
            racha++;
            cursor = cursor.minusDays(1);
        }
        return racha;
    }
}
