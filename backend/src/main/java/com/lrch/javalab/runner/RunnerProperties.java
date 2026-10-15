package com.lrch.javalab.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "javalab.runner")
public record RunnerProperties(
        Boolean useDocker,
        String image,
        long timeoutSeconds,
        String memory,
        String cpus,
        String junitJar,
        int maxConcurrent,
        long queueWaitSeconds,
        String workDir,
        String hostWorkDir) {

    private static final Logger log = LoggerFactory.getLogger(RunnerProperties.class);

    /**
     * Tope de seguridad: por encima de esto el navegador (y los proxies delante) cortan la peticion
     * antes y el usuario ve un error de red. Ademas, con maxConcurrent=1 un ciclo infinito bloquearia
     * el runner todo ese tiempo.
     */
    private static final long TIMEOUT_MAXIMO_SEGUNDOS = 60;

    public RunnerProperties {
        if (image == null || image.isBlank()) {
            image = "javalab-runner:17";
        }
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 10;
        } else if (timeoutSeconds > TIMEOUT_MAXIMO_SEGUNDOS) {
            log.warn("JAVALAB_RUNNER_TIMEOUT_SECONDS={} supera el maximo de {}s: se recorta a {}s "
                            + "(un valor mayor no llega a usarse, el navegador corta antes)",
                    timeoutSeconds, TIMEOUT_MAXIMO_SEGUNDOS, TIMEOUT_MAXIMO_SEGUNDOS);
            timeoutSeconds = TIMEOUT_MAXIMO_SEGUNDOS;
        }
        if (memory == null || memory.isBlank()) {
            memory = "256m";
        }
        if (cpus == null || cpus.isBlank()) {
            cpus = "0.5";
        }
        if (maxConcurrent <= 0) {
            maxConcurrent = 1;
        }
        if (queueWaitSeconds <= 0) {
            queueWaitSeconds = 60;
        }
        if (junitJar == null || junitJar.isBlank()) {
            junitJar = "../runner/junit-platform-console-standalone.jar";
        }
    }

    public boolean isUseDocker() {
        return !Boolean.FALSE.equals(useDocker);
    }

    public Path junitJarPath() {
        return Paths.get(junitJar).toAbsolutePath().normalize();
    }

    /**
     * Directorio donde se preparan los archivos de cada envio. Con Docker, el backend monta este
     * directorio y los runners lo reciben con la MISMA ruta (por eso debe ser uno compartido, no un
     * temporal del contenedor: el daemon del host no ve los temporales del backend). Si esta vacio,
     * se usa el temporal del sistema (vale para el motor local y para desarrollo).
     */
    public Path workDirPath() {
        if (workDir == null || workDir.isBlank()) {
            return null;
        }
        return Paths.get(workDir).toAbsolutePath().normalize();
    }

    /**
     * Ruta del directorio de trabajo tal como la ve el DAEMON de Docker (el host), si es distinta de
     * {@link #workDir()}. El daemon resuelve los {@code -v} contra el host, no contra el contenedor
     * del backend: por eso al montar en el runner hay que usar la ruta del host. Si esta vacio, se
     * asume que {@code workDir} ya es una ruta valida en el host (por ejemplo, cuando el backend corre
     * directamente en la maquina del host, sin contenedor de por medio).
     */
    public String hostWorkDirOrDefault() {
        if (hostWorkDir != null && !hostWorkDir.isBlank()) {
            return hostWorkDir;
        }
        return workDir;
    }
}
