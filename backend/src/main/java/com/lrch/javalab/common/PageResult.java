package com.lrch.javalab.common;

import java.util.List;

/** Resultado de una consulta paginada, antes de convertirlo al contrato de respuesta. */
public record PageResult<T>(List<T> items, long total, int page, int perPage) {

    public Pagination pagination() {
        return Pagination.of(total, page, perPage);
    }
}
