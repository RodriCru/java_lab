package com.lrch.javalab.runner;

import org.springframework.stereotype.Component;

import com.lrch.javalab.runner.lang.LanguageAdapter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Ejecuta el codigo del alumno dentro de un contenedor Docker desechable y sin privilegios.
 */
@Component
public class DockerCodeRunner extends AbstractJUnitRunner {

    private static final String DOCKER = "docker";

    private volatile Boolean disponible;

    public DockerCodeRunner(RunnerProperties properties) {
        super(properties);
    }

    @Override
    public String nombre() {
        return "docker";
    }

    @Override
    public boolean disponible() {
        Boolean cacheado = disponible;
        if (cacheado == null) {
            cacheado = comprobarImagen();
            disponible = cacheado;
            if (!cacheado) {
                log.warn("El motor Docker no esta disponible. Imagen '{}' no encontrada. "
                        + "Ejecuta 'runner/build.sh' o revisa 'docker info'.", properties.image());
            }
        }
        return cacheado;
    }

    private boolean comprobarImagen() {
        try {
            Process proceso = new ProcessBuilder(DOCKER, "image", "inspect", properties.image())
                    .redirectErrorStream(true)
                    .start();
            try (var entrada = proceso.getInputStream()) {
                entrada.readAllBytes();
            }
            return proceso.waitFor(20, TimeUnit.SECONDS) && proceso.exitValue() == 0;
        } catch (IOException ex) {
            log.debug("Docker no responde: {}", ex.getMessage());
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    protected SalidaProceso ejecutarProceso(Path srcDir, Path outDir, LanguageAdapter adapter) throws IOException {
        String contenedor = "javalab-" + UUID.randomUUID();

        List<String> comando = new ArrayList<>(List.of(
                DOCKER, "run", "--rm",
                "--name", contenedor,
                "--network", "none",
                "--memory", properties.memory(),
                "--cpus", properties.cpus(),
                "--pids-limit", "64",
                "--cap-drop", "ALL",
                "--security-opt", "no-new-privileges",
                "--read-only",
                "--tmpfs", "/tmp:rw,size=64m,mode=1777",
                "-v", srcDir.toAbsolutePath() + ":/in:ro",
                "-v", outDir.toAbsolutePath() + ":/out:rw",
                adapter.image()));

        return lanzar(comando, outDir, () -> matarContenedor(contenedor));
    }

    /** El timeout de 'docker run' puede dejar el contenedor vivo: hay que matarlo aparte. */
    private void matarContenedor(String contenedor) {
        try {
            Process proceso = new ProcessBuilder(DOCKER, "kill", contenedor)
                    .redirectErrorStream(true)
                    .start();
            try (var entrada = proceso.getInputStream()) {
                entrada.readAllBytes();
            }
            proceso.waitFor(10, TimeUnit.SECONDS);
        } catch (IOException ex) {
            log.debug("No se pudo matar el contenedor {}: {}", contenedor, ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
