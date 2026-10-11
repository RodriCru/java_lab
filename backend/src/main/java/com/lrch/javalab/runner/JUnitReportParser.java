package com.lrch.javalab.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Lee los reportes XML que genera JUnit Platform y los convierte en resultados por test.
 *
 * <p>Se queda con el reporte que tiene mas tests, porque JUnit escribe un archivo por motor
 * (jupiter, vintage, platform-suite) y solo uno contiene los casos reales.
 */
public class JUnitReportParser {

    private static final Logger log = LoggerFactory.getLogger(JUnitReportParser.class);

    public List<TestCaseOutcome> parse(Path reportsDir) {
        if (reportsDir == null || !Files.isDirectory(reportsDir)) {
            return List.of();
        }

        try (Stream<Path> archivos = Files.list(reportsDir)) {
            Path elegido = archivos
                    .filter(p -> p.getFileName().toString().startsWith("TEST-"))
                    .filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .map(this::conTotalDeTests)
                    .filter(p -> p.total() > 0)
                    .max(Comparator.comparingLong(ArchivoConTotal::total))
                    .map(ArchivoConTotal::archivo)
                    .orElse(null);

            return elegido == null ? List.of() : leerTestCases(elegido);
        } catch (IOException ex) {
            log.warn("No se pudieron leer los reportes de JUnit: {}", ex.getMessage());
            return List.of();
        }
    }

    private record ArchivoConTotal(Path archivo, long total) {
    }

    private ArchivoConTotal conTotalDeTests(Path archivo) {
        try {
            Document doc = parsearDocumento(archivo);
            String tests = doc.getDocumentElement().getAttribute("tests");
            return new ArchivoConTotal(archivo, tests.isBlank() ? 0L : Long.parseLong(tests));
        } catch (Exception ex) {
            return new ArchivoConTotal(archivo, 0L);
        }
    }

    private List<TestCaseOutcome> leerTestCases(Path archivo) throws IOException {
        List<TestCaseOutcome> resultados = new ArrayList<>();
        try {
            Document doc = parsearDocumento(archivo);
            NodeList casos = doc.getElementsByTagName("testcase");
            for (int i = 0; i < casos.getLength(); i++) {
                Element caso = (Element) casos.item(i);
                boolean skipped = caso.getElementsByTagName("skipped").getLength() > 0;
                Element fallo = primerElemento(caso, "failure", "error");
                resultados.add(new TestCaseOutcome(
                        limpia(caso.getAttribute("name")),
                        fallo == null,
                        skipped,
                        fallo == null ? null : mensajeDe(fallo),
                        milisegundos(caso.getAttribute("time")),
                        null,
                        null));
            }
        } catch (ParserConfigurationException | SAXException ex) {
            log.warn("Reporte de JUnit ilegible ({}): {}", archivo.getFileName(), ex.getMessage());
        }
        return resultados;
    }

    private Document parsearDocumento(Path archivo) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(archivo.toFile());
    }

    private Element primerElemento(Element padre, String... etiquetas) {
        for (String etiqueta : etiquetas) {
            NodeList encontrados = padre.getElementsByTagName(etiqueta);
            if (encontrados.getLength() > 0) {
                return (Element) encontrados.item(0);
            }
        }
        return null;
    }

    private String mensajeDe(Element fallo) {
        String mensaje = fallo.getAttribute("message");
        if (mensaje != null && !mensaje.isBlank()) {
            return limpiarMensaje(mensaje);
        }
        String texto = fallo.getTextContent();
        return texto == null || texto.isBlank() ? "El test no paso." : limpiarMensaje(texto);
    }

    /** Quita los saltos de linea y los "==&gt;" que mete el formateador de aserciones. */
    private String limpiarMensaje(String mensaje) {
        return mensaje.replace("==>", "->").replaceAll("\\s+", " ").trim();
    }

    private String limpia(String nombre) {
        return nombre != null && nombre.endsWith("()") ? nombre.substring(0, nombre.length() - 2) : nombre;
    }

    private long milisegundos(String segundos) {
        try {
            return Math.round(Double.parseDouble(segundos) * 1000);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
