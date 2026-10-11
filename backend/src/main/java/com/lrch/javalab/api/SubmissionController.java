package com.lrch.javalab.api;

import com.lrch.javalab.auth.AuthenticatedUser;
import com.lrch.javalab.catalog.CatalogService;
import com.lrch.javalab.catalog.Exercise;
import com.lrch.javalab.common.ApiResponse;
import com.lrch.javalab.progress.ProgressService;
import com.lrch.javalab.runner.ExecutionRequest;
import com.lrch.javalab.runner.ExecutionResult;
import com.lrch.javalab.runner.RunnerService;
import com.lrch.javalab.runner.SourceValidator;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Envio de una solucion: valida el codigo, lo compila y ejecuta los tests del ejercicio,
 * y guarda el intento en el progreso del usuario.
 */
@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final CatalogService catalogService;
    private final SourceValidator sourceValidator;
    private final RunnerService runnerService;
    private final ProgressService progressService;

    public SubmissionController(CatalogService catalogService,
                                SourceValidator sourceValidator,
                                RunnerService runnerService,
                                ProgressService progressService) {
        this.catalogService = catalogService;
        this.sourceValidator = sourceValidator;
        this.runnerService = runnerService;
        this.progressService = progressService;
    }

    @PostMapping
    public ApiResponse<SubmissionResponse> enviar(@AuthenticationPrincipal AuthenticatedUser usuario,
                                                  @Valid @RequestBody SubmissionRequest solicitud) {
        Exercise ejercicio = catalogService.buscar(solicitud.exerciseSlug());
        sourceValidator.validar(solicitud.sourceCode(), ejercicio.className());

        boolean yaEstabaResuelto = progressService.slugsResueltos(usuario.id()).contains(ejercicio.slug());

        ExecutionResult resultado = runnerService.ejecutar(
                new ExecutionRequest(ejercicio.className(), solicitud.sourceCode(), ejercicio.tests()));

        boolean resuelto = progressService.registrarIntento(
                usuario.id(), ejercicio, solicitud.sourceCode(), resultado);

        return ApiResponse.ok(
                SubmissionResponse.from(ejercicio, resultado, resuelto, yaEstabaResuelto,
                        solicitud.complexityAnswer()),
                mensaje(resultado, resuelto, yaEstabaResuelto));
    }

    private String mensaje(ExecutionResult resultado, boolean resuelto, boolean yaEstabaResuelto) {
        return switch (resultado.status()) {
            case PASSED -> yaEstabaResuelto
                    ? "¡Correcto! Los " + resultado.tests().size() + " tests siguen pasando."
                    : "¡Bien hecho! Resolviste el ejercicio: pasaron los " + resultado.tests().size() + " tests.";
            case FAILED -> "Pasaron " + resultado.passedCount() + " de " + resultado.tests().size()
                    + " tests. Revisa los que fallaron.";
            case COMPILE_ERROR -> "Tu código no compiló. Revisa los errores de compilación.";
            case TIMEOUT -> "Tu solución tardó demasiado y se canceló. Revisa si tienes un ciclo infinito.";
            case INTERNAL_ERROR -> "No se pudieron ejecutar los tests. Inténtalo de nuevo.";
        };
    }
}
