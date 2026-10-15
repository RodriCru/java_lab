package com.lrch.javalab.runner;

import com.lrch.javalab.common.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RunnerServiceTest {

    private static final ExecutionRequest PETICION = peticionJava();

    /** Petición de un ejercicio Java mínimo: el adaptador no se usa porque el motor está mockeado. */
    private static ExecutionRequest peticionJava() {
        var ejercicio = new com.lrch.javalab.catalog.Exercise("java", "slug", 1, "topic", "Titulo",
                "enunciado", "public static int suma(int a, int b)", "Solution", 5, "easy",
                List.of("pista 1", "pista 2"), "class Solution {}", "class Solution {}", "@Test", null);
        var adapter = new com.lrch.javalab.runner.lang.JavaLanguageAdapter(
                new com.lrch.javalab.runner.SourceValidator());
        return new ExecutionRequest(ejercicio, adapter, "codigo");
    }

    private static ExecutionResult resultado() {
        return new ExecutionResult(ExecutionResult.Status.PASSED, true, List.of(), "", "", 0, "local");
    }

    private RunnerService servicio(LocalCodeRunner local, int maxConcurrent, long esperaSegundos) {
        DockerCodeRunner docker = mock(DockerCodeRunner.class);
        when(docker.disponible()).thenReturn(false);
        when(local.disponible()).thenReturn(true);
        RunnerProperties propiedades = new RunnerProperties(false, null, 10, null, null, null,
                maxConcurrent, esperaSegundos);
        return new RunnerService(docker, local, propiedades);
    }

    @Test
    @DisplayName("Los envíos simultáneos se ejecutan de uno en uno")
    void unoALaVez() throws Exception {
        LocalCodeRunner local = mock(LocalCodeRunner.class);
        AtomicInteger enCurso = new AtomicInteger();
        AtomicInteger maximo = new AtomicInteger();
        when(local.ejecutar(any())).thenAnswer(invocacion -> {
            int ahora = enCurso.incrementAndGet();
            maximo.accumulateAndGet(ahora, Math::max);
            Thread.sleep(150);
            enCurso.decrementAndGet();
            return resultado();
        });
        RunnerService servicio = servicio(local, 1, 30);

        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<ExecutionResult>> futuros = java.util.stream.IntStream.range(0, 4)
                    .mapToObj(i -> pool.submit(() -> servicio.ejecutar(PETICION))).toList();
            for (Future<ExecutionResult> futuro : futuros) {
                assertThat(futuro.get(10, TimeUnit.SECONDS).status()).isEqualTo(ExecutionResult.Status.PASSED);
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(maximo.get()).as("envíos ejecutándose a la vez").isEqualTo(1);
    }

    @Test
    @DisplayName("Si la espera se agota responde 429 SERVIDOR_OCUPADO y no deja el turno ocupado")
    void esperaAgotada() throws Exception {
        LocalCodeRunner local = mock(LocalCodeRunner.class);
        CountDownLatch dentro = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        when(local.ejecutar(any())).thenAnswer(invocacion -> {
            dentro.countDown();
            soltar.await(10, TimeUnit.SECONDS);
            return resultado();
        });
        RunnerService servicio = servicio(local, 1, 1);

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<ExecutionResult> primero = pool.submit(() -> servicio.ejecutar(PETICION));
            assertThat(dentro.await(5, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> servicio.ejecutar(PETICION))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SERVIDOR_OCUPADO");

            soltar.countDown();
            primero.get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        // El turno se liberó: un envío nuevo vuelve a entrar sin esperar.
        assertThat(servicio.ejecutar(PETICION).status()).isEqualTo(ExecutionResult.Status.PASSED);
    }

    @Test
    @DisplayName("Si el motor lanza una excepción el turno se libera igual")
    void liberaElTurnoSiFalla() {
        LocalCodeRunner local = mock(LocalCodeRunner.class);
        when(local.ejecutar(any())).thenThrow(new IllegalStateException("fallo")).thenReturn(resultado());
        RunnerService servicio = servicio(local, 1, 1);

        assertThatThrownBy(() -> servicio.ejecutar(PETICION)).isInstanceOf(IllegalStateException.class);
        assertThat(servicio.ejecutar(PETICION).status()).isEqualTo(ExecutionResult.Status.PASSED);
    }
}
