package com.lrch.javalab.api;

import com.lrch.javalab.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> salud() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("status", "ok");
        datos.put("service", "javalab-backend");
        datos.put("timestamp", Instant.now().toString());
        return ApiResponse.ok(datos);
    }
}
