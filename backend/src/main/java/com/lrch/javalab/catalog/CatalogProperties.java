package com.lrch.javalab.catalog;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "javalab.catalog")
public record CatalogProperties(String exercisesDir, String questionsDir) {

    public CatalogProperties {
        if (exercisesDir == null || exercisesDir.isBlank()) {
            exercisesDir = "../exercises";
        }
        if (questionsDir == null || questionsDir.isBlank()) {
            questionsDir = "../questions";
        }
    }
}
