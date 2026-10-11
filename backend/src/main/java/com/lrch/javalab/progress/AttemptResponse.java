package com.lrch.javalab.progress;

import java.time.Instant;

public record AttemptResponse(
        String exerciseSlug,
        String exerciseTitle,
        int level,
        boolean passed,
        String status,
        int testsPassed,
        int testsTotal,
        Instant createdAt) {
}
