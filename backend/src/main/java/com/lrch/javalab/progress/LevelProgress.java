package com.lrch.javalab.progress;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Avance del usuario dentro de un nivel. */
public record LevelProgress(int level, int total, int solved) {

    /** Porcentaje resuelto. Se anota porque Jackson no expone los metodos extra de un record. */
    @JsonProperty("percent")
    public int percent() {
        return total == 0 ? 0 : (int) Math.round(solved * 100.0 / total);
    }
}
