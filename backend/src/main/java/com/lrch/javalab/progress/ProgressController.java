package com.lrch.javalab.progress;

import com.lrch.javalab.auth.AuthenticatedUser;
import com.lrch.javalab.common.ApiResponse;
import com.lrch.javalab.common.Pagination;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
public class ProgressController {

    private static final int PER_PAGE_POR_DEFECTO = 20;
    private static final int PER_PAGE_MAXIMO = 100;

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ApiResponse<ProgressSummary> resumen(@AuthenticationPrincipal AuthenticatedUser usuario,
                                                @RequestParam(required = false) String language) {
        String lenguaje = language == null || language.isBlank()
                ? null : language.trim().toLowerCase(java.util.Locale.ROOT);
        return ApiResponse.ok(progressService.resumen(usuario.id(), lenguaje));
    }

    @GetMapping("/history")
    public ApiResponse<List<AttemptResponse>> historial(
            @AuthenticationPrincipal AuthenticatedUser usuario,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "" + PER_PAGE_POR_DEFECTO) int perPage) {

        int pagina = Math.max(page, 1);
        int tamano = Math.min(Math.max(perPage, 1), PER_PAGE_MAXIMO);

        List<AttemptResponse> intentos = progressService.historial(usuario.id(), pagina, tamano);
        long total = progressService.totalIntentos(usuario.id());
        return ApiResponse.page(intentos, Pagination.of(total, pagina, tamano));
    }
}
