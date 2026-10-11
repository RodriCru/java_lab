package com.lrch.javalab.catalog;

public record HintResponse(
        String slug,
        int hintIndex,
        String hint,
        int totalHints,
        boolean hasMore) {
}
