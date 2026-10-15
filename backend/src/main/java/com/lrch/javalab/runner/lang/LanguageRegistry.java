package com.lrch.javalab.runner.lang;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve el {@link LanguageAdapter} de un lenguaje por su identificador.
 *
 * <p>Cada lenguaje se registra como un bean de Spring; aqui solo se indexan por {@code language()}.
 * Si un ejercicio declara un lenguaje sin adaptador, la ejecucion falla con un mensaje claro en vez
 * de intentar compilarlo como Java.
 */
@Component
public class LanguageRegistry {

    private final Map<String, LanguageAdapter> adapters;

    public LanguageRegistry(List<LanguageAdapter> adapters) {
        this.adapters = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(LanguageAdapter::language, Function.identity()));
    }

    public Optional<LanguageAdapter> porLenguaje(String language) {
        String clave = language == null || language.isBlank()
                ? "java" : language.trim().toLowerCase(java.util.Locale.ROOT);
        return Optional.ofNullable(adapters.get(clave));
    }

    public List<String> lenguajesSoportados() {
        return adapters.keySet().stream().sorted().toList();
    }
}
