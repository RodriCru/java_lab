package com.lrch.javalab.runnerservice;

import com.lrch.javalab.runner.ExecutionResult;
import com.lrch.javalab.runner.lang.RunnerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Unico endpoint del runner-service: recibe un envio y devuelve el resultado test por test.
 *
 * <p>Va en la red interna (el backend lo llama por su nombre en Coolify); no se expone al navegador.
 */
@RestController
@RequestMapping("/internal")
public class RunController {

    private final RunnerEngine engine;

    public RunController(RunnerEngine engine) {
        this.engine = engine;
    }

    @PostMapping("/run")
    public ExecutionResult run(@RequestBody RunRequest request) {
        return engine.ejecutar(request);
    }

    @ExceptionHandler(RunnerException.class)
    public ResponseEntity<Map<String, String>> errorDeValidacion(RunnerException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("code", ex.getCode(), "message", ex.getMessage()));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
