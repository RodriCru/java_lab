package com.lrch.javalab.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato unico de respuesta de la API, segun el estandar LRCH.
 *
 * <pre>
 * exito    -> { "success": true,  "data": {...}, "message": "..." }
 * error    -> { "success": false, "error": "CODIGO_EN_MAYUSCULAS", "message": "..." }
 * paginado -> { "success": true,  "data": [...], "pagination": { ... } }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String error;
    private final Pagination pagination;

    private ApiResponse(boolean success, T data, String message, String error, Pagination pagination) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
        this.pagination = pagination;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, null);
    }

    public static <T> ApiResponse<T> page(T data, Pagination pagination) {
        return new ApiResponse<>(true, data, null, null, pagination);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, message, code, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
