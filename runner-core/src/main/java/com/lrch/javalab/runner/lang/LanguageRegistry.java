package com.lrch.javalab.runner.lang;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve el {@link LanguageAdapter} de un lenguaje por su identificador.
 *
 * <p>Los adaptadores se registran aqui (no depende de Spring): el nucleo lo usan tanto el backend
 * como el runner-service. Si un ejercicio declara un lenguaje sin adaptador, se falla con un mensaje
 * claro en vez de intentar ejecutarlo mal.
 */
public class LanguageRegistry {

    private final Map<String, LanguageAdapter> adapters;
    private final LanguageAdapter porDefecto;

    public LanguageRegistry(LanguageAdapter... adapters) {
        this.adapters = List.of(adapters).stream()
                .collect(Collectors.toUnmodifiableMap(LanguageAdapter::language, Function.identity()));
        this.porDefecto = this.adapters.get("java");
    }

    public Optional<LanguageAdapter> porLenguaje(String language) {
        String clave = language == null || language.isBlank()
                ? "java" : language.trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(adapters.get(clave));
    }

    public List<String> lenguajesSoportados() {
        return adapters.keySet().stream().sorted().toList();
    }

    /** Adaptador de Java; util como valor por defecto cuando el ejercicio no declara lenguaje. */
    public LanguageAdapter porDefecto() {
        return porDefecto;
    }
}
