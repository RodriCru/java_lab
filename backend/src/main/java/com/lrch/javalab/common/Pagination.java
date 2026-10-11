package com.lrch.javalab.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Bloque de paginacion del contrato LRCH: total, page, per_page, total_pages. */
public record Pagination(
        long total,
        int page,
        @JsonProperty("per_page") int perPage,
        @JsonProperty("total_pages") int totalPages) {

    public static Pagination of(long total, int page, int perPage) {
        int totalPages = perPage <= 0 ? 0 : (int) Math.ceil((double) total / perPage);
        return new Pagination(total, page, perPage, totalPages);
    }
}
