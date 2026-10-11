package com.lrch.javalab.catalog;

import com.lrch.javalab.auth.AuthenticatedUser;
import com.lrch.javalab.common.ApiResponse;
import com.lrch.javalab.common.PageResult;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private static final int PER_PAGE_POR_DEFECTO = 20;
    private static final int PER_PAGE_MAXIMO = 100;

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/levels")
    public ApiResponse<List<LevelResponse>> niveles(@AuthenticationPrincipal AuthenticatedUser usuario) {
        return ApiResponse.ok(catalogService.niveles(usuario.id()));
    }

    @GetMapping("/exercises")
    public ApiResponse<List<ExerciseSummary>> listar(
            @AuthenticationPrincipal AuthenticatedUser usuario,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false, name = "status") String estado,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "" + PER_PAGE_POR_DEFECTO) int perPage) {

        int pagina = Math.max(page, 1);
        int tamano = Math.min(Math.max(perPage, 1), PER_PAGE_MAXIMO);

        PageResult<ExerciseSummary> resultado =
                catalogService.listar(usuario.id(), level, topic, estado, search, pagina, tamano);
        return ApiResponse.page(resultado.items(), resultado.pagination());
    }

    @GetMapping("/exercises/{slug}")
    public ApiResponse<ExerciseDetail> detalle(@AuthenticationPrincipal AuthenticatedUser usuario,
                                               @PathVariable String slug) {
        return ApiResponse.ok(catalogService.detalle(usuario.id(), slug));
    }

    @GetMapping("/exercises/{slug}/hints/{index}")
    public ApiResponse<HintResponse> pista(@PathVariable String slug, @PathVariable int index) {
        return ApiResponse.ok(catalogService.pista(slug, index));
    }
}
