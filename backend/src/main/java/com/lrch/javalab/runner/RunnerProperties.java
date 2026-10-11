package com.lrch.javalab.runner;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "javalab.runner")
public record RunnerProperties(
        Boolean useDocker,
        String image,
        long timeoutSeconds,
        String memory,
        String cpus,
        String junitJar,
        int maxConcurrent,
        long queueWaitSeconds) {

    public RunnerProperties {
        if (image == null || image.isBlank()) {
            image = "javalab-runner:17";
        }
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 10;
        }
        if (memory == null || memory.isBlank()) {
            memory = "256m";
        }
        if (cpus == null || cpus.isBlank()) {
            cpus = "0.5";
        }
        if (maxConcurrent <= 0) {
            maxConcurrent = 1;
        }
        if (queueWaitSeconds <= 0) {
            queueWaitSeconds = 60;
        }
        if (junitJar == null || junitJar.isBlank()) {
            junitJar = "../runner/junit-platform-console-standalone.jar";
        }
    }

    public boolean isUseDocker() {
        return !Boolean.FALSE.equals(useDocker);
    }

    public Path junitJarPath() {
        return Paths.get(junitJar).toAbsolutePath().normalize();
    }
}
