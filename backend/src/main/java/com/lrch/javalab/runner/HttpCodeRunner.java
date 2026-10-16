package com.lrch.javalab.runner;

import com.lrch.javalab.catalog.Exercise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Motor de ejecucion del backend: le manda el envio al runner-service por HTTP y devuelve su
 * resultado. El backend ya NO habla con Docker (eso vive en el runner-service).
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "javalab.runner", name = "service-enabled", havingValue = "true", matchIfMissing = true)
public class HttpCodeRunner implements CodeRunner {

    private static final Logger log = LoggerFactory.getLogger(HttpCodeRunner.class);

    private final RunnerProperties properties;
    private final RestClient cliente;

    public HttpCodeRunner(RunnerProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.cliente = builder.baseUrl(properties.serviceUrl()).build();
    }

    @Override
    public String nombre() {
        return "service";
    }

    @Override
    public boolean disponible() {
        try {
            Map<?, ?> salud = cliente.get().uri("/internal/health").retrieve().body(Map.class);
            return salud != null && "ok".equals(salud.get("status"));
        } catch (RuntimeException ex) {
            log.warn("El runner-service no responde en {}: {}", properties.serviceUrl(), ex.getMessage());
            return false;
        }
    }

    @Override
    public ExecutionResult ejecutar(ExecutionRequest request) {
        Exercise e = request.ejercicio();
        Map<String, String> cuerpo = Map.of(
                "language", e.language(),
                "className", e.className() == null ? "" : e.className(),
                "tests", e.tests(),
                "sourceCode", request.studentCode());

        return cliente.post()
                .uri("/internal/run")
                .body(cuerpo)
                .retrieve()
                .body(ExecutionResult.class);
    }
}
