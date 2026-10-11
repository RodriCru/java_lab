package com.lrch.javalab.catalog;

import java.util.List;

public record LevelResponse(int level, String name, int total, int solved, int percent,
                            List<String> topics, List<TopicSection> sections) {
}
