package com.lrch.javalab.api;

import com.lrch.javalab.common.ApiException;
import com.lrch.javalab.runner.CodeRunner;
import com.lrch.javalab.runner.ExecutionRequest;
import com.lrch.javalab.runner.ExecutionResult;
import com.lrch.javalab.runner.TestCaseOutcome;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * Sustituye el motor real por uno de mentira en los tests del backend.
 *
 * <p>Los tests de la API (catalogo, progreso, complejidad...) no deben depender de un
 * runner-service arrancado: aqui se devuelve un resultado controlado. La ejecucion REAL de codigo
 * se prueba en el modulo {@code runner-service}, que es donde vive el motor.
 *
 * <p>Criterio del doble:
 * <ul>
 *   <li>el codigo contiene {@code PROHIBIDO} -> se rechaza (400 CODIGO_NO_PERMITIDO)</li>
 *   <li>el codigo contiene {@code ROMPE} -> COMPILE_ERROR</li>
 *   <li>el codigo contiene {@code FALLA} -> FAILED</li>
 *   <li>en otro caso -> PASSED con un test</li>
 * </ul>
 */
@TestConfiguration
public class MotorDeMentiraConfig {

    @Bean
    @Primary
    public CodeRunner motorDeMentira() {
        return new CodeRunner() {
            @Override
            public String nombre() {
                return "stub";
            }

            @Override
            public boolean disponible() {
                return true;
            }

            @Override
            public ExecutionResult ejecutar(ExecutionRequest request) {
                String codigo = request.studentCode() == null ? "" : request.studentCode();
                if (codigo.contains("PROHIBIDO")) {
                    throw ApiException.badRequest("CODIGO_NO_PERMITIDO",
                            "No se puede ejecutar tu código: no está permitido usar 'java.io.File'.");
                }
                if (codigo.contains("ROMPE")) {
                    return new ExecutionResult(ExecutionResult.Status.COMPILE_ERROR, false,
                            List.of(), "error de compilacion simulado", null, 0, "stub");
                }
                boolean falla = codigo.contains("FALLA");
                List<TestCaseOutcome> tests = new ArrayList<>();
                tests.add(new TestCaseOutcome("t1", !falla, false, falla ? "assert falló" : null, 0, null, null));
                return new ExecutionResult(
                        falla ? ExecutionResult.Status.FAILED : ExecutionResult.Status.PASSED,
                        true, tests, null, "", 0, "stub");
            }
        };
    }
}
