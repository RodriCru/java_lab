package com.lrch.javalab.runner;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "javalab.runner")
public record RunnerProperties(
        String serviceUrl,
        long timeoutSeconds,
        int maxConcurrent,
        long queueWaitSeconds) {

    /**
     * Tope de seguridad: por encima de esto el navegador (y los proxies delante) cortan la peticion
     * antes y el usuario ve un error de red. Ademas, con maxConcurrent=1 un ciclo infinito bloquearia
     * el runner todo ese tiempo.
     */
    private static final long TIMEOUT_MAXIMO_SEGUNDOS = 60;

    public RunnerProperties {
        if (serviceUrl == null || serviceUrl.isBlank()) {
            serviceUrl = "http://localhost:8081";
        }
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 10;
        } else if (timeoutSeconds > TIMEOUT_MAXIMO_SEGUNDOS) {
            timeoutSeconds = TIMEOUT_MAXIMO_SEGUNDOS;
        }
        if (maxConcurrent <= 0) {
            maxConcurrent = 1;
        }
        if (queueWaitSeconds <= 0) {
            queueWaitSeconds = 60;
        }
    }
}
