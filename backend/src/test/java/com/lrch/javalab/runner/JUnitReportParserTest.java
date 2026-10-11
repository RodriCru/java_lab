package com.lrch.javalab.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JUnitReportParserTest {

    private final JUnitReportParser parser = new JUnitReportParser();

    @Test
    @DisplayName("Interpreta un reporte con tests que pasan y que fallan")
    void interpretaTestsPasadosYFallidos(@TempDir Path directorio) throws Exception {
        Files.writeString(directorio.resolve("TEST-junit-jupiter.xml"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <testsuite name="JUnit Jupiter" tests="3" skipped="0" failures="1" errors="0" time="0.05">
                <testcase name="pasa()" classname="SolutionTest" time="0.01"></testcase>
                <testcase name="falla()" classname="SolutionTest" time="0.02">
                <failure message="esperaba 5 pero fue 2 ==&gt; expected: &lt;5&gt; but was: &lt;2&gt;" type="org.opentest4j.AssertionFailedError"><![CDATA[detalle]]></failure>
                </testcase>
                <testcase name="seOmite()" classname="SolutionTest" time="0">
                <skipped/>
                </testcase>
                </testsuite>
                """);

        List<TestCaseOutcome> resultados = parser.parse(directorio);

        assertThat(resultados).hasSize(3);
        assertThat(resultados.get(0).name()).isEqualTo("pasa");
        assertThat(resultados.get(0).passed()).isTrue();
        assertThat(resultados.get(0).durationMillis()).isEqualTo(10);

        assertThat(resultados.get(1).name()).isEqualTo("falla");
        assertThat(resultados.get(1).passed()).isFalse();
        assertThat(resultados.get(1).message()).contains("esperaba 5 pero fue 2").doesNotContain("==>");

        assertThat(resultados.get(2).name()).isEqualTo("seOmite");
        assertThat(resultados.get(2).skipped()).isTrue();
    }

    @Test
    @DisplayName("Ignora los reportes vacios de otros motores")
    void ignoraLosReportesSinTests(@TempDir Path directorio) throws Exception {
        Files.writeString(directorio.resolve("TEST-junit-jupiter.xml"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <testsuite name="JUnit Jupiter" tests="1" skipped="0" failures="0" errors="0" time="0.01">
                <testcase name="unico()" classname="SolutionTest" time="0.01"></testcase>
                </testsuite>
                """);
        Files.writeString(directorio.resolve("TEST-junit-vintage.xml"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <testsuite name="JUnit Vintage" tests="0" skipped="0" failures="0" errors="0" time="0.0"></testsuite>
                """);

        List<TestCaseOutcome> resultados = parser.parse(directorio);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).name()).isEqualTo("unico");
    }

    @Test
    @DisplayName("Devuelve una lista vacia si no hay reportes")
    void sinReportesDevuelveVacio(@TempDir Path directorio) {
        assertThat(parser.parse(directorio)).isEmpty();
        assertThat(parser.parse(directorio.resolve("no-existe"))).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }
}
