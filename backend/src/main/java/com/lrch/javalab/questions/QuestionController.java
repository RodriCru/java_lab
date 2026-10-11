package com.lrch.javalab.questions;

import com.lrch.javalab.auth.AuthenticatedUser;
import com.lrch.javalab.common.ApiResponse;
import com.lrch.javalab.common.PageResult;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private static final int PER_PAGE_MAXIMO = 100;

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @GetMapping("/topics")
    public ApiResponse<List<TopicSummary>> temas(@AuthenticationPrincipal AuthenticatedUser usuario) {
        return ApiResponse.ok(service.temas(usuario.id()));
    }

    @GetMapping
    public ApiResponse<List<QuestionView>> listar(
            @AuthenticationPrincipal AuthenticatedUser usuario,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean random,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "20") int perPage) {
        int pagina = Math.max(page, 1);
        int tamano = Math.min(Math.max(perPage, 1), PER_PAGE_MAXIMO);
        PageResult<QuestionView> resultado =
                service.listar(usuario.id(), topic, difficulty, status, search, random, pagina, tamano);
        return ApiResponse.page(resultado.items(), resultado.pagination());
    }

    @PostMapping("/{id}/answer")
    public ApiResponse<AnswerResponse> responder(@AuthenticationPrincipal AuthenticatedUser usuario,
                                                 @PathVariable String id,
                                                 @RequestBody AnswerRequest solicitud) {
        return ApiResponse.ok(service.responder(usuario.id(), id, solicitud.selectedIndex()));
    }
}
