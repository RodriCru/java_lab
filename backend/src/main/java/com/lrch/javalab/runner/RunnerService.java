package com.lrch.javalab.runner;

import com.lrch.javalab.common.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Punto de entrada unico del backend hacia la ejecucion: encola los envios (para no saturar el
 * runner-service) y delega en {@link HttpCodeRunner}.
 */
@Service
public class RunnerService {

    private static final Logger log = LoggerFactory.getLogger(RunnerService.class);

    private final CodeRunner runner;
    private final RunnerProperties properties;

    /** Cola de envios: cada compilacion consume CPU y memoria; en VPS conviene ir de uno en uno. */
    private final Semaphore turnos;

    public RunnerService(CodeRunner runner, RunnerProperties properties) {
        this.runner = runner;
        this.properties = properties;
        this.turnos = new Semaphore(properties.maxConcurrent(), true);
    }

    public ExecutionResult ejecutar(ExecutionRequest request) {
        esperarTurno();
        try {
            return runner.ejecutar(request);
        } finally {
            turnos.release();
        }
    }

    private void esperarTurno() {
        boolean obtenido;
        try {
            obtenido = turnos.tryAcquire(properties.queueWaitSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            obtenido = false;
        }
        if (!obtenido) {
            throw ApiException.tooManyRequests("SERVIDOR_OCUPADO",
                    "Hay otros envíos en curso. Inténtalo de nuevo en unos segundos.");
        }
    }

    /** Motor efectivamente en uso, para mostrarlo en la respuesta de la API. */
    public String motorActivo() {
        return runner.nombre();
    }
}
