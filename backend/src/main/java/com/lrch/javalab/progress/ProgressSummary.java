package com.lrch.javalab.progress;

import java.util.List;

public record ProgressSummary(
        long xp,
        int solved,
        int total,
        int streakDays,
        int attempts,
        List<LevelProgress> levels) {
}
