package com.lrch.javalab.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Un intento de resolver un ejercicio. Es el historial: nunca se borra. */
@Entity
@Table(name = "attempts")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exercise_slug", nullable = false)
    private String exerciseSlug;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Column(name = "tests_passed", nullable = false)
    private int testsPassed;

    @Column(name = "tests_total", nullable = false)
    private int testsTotal;

    /** PASSED, FAILED, COMPILE_ERROR, TIMEOUT o INTERNAL_ERROR. */
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    protected Attempt() {
    }

    public Attempt(Long userId, String exerciseSlug, boolean passed, int testsPassed, int testsTotal, String status) {
        this.userId = userId;
        this.exerciseSlug = exerciseSlug;
        this.passed = passed;
        this.testsPassed = testsPassed;
        this.testsTotal = testsTotal;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getExerciseSlug() {
        return exerciseSlug;
    }

    public boolean isPassed() {
        return passed;
    }

    public int getTestsPassed() {
        return testsPassed;
    }

    public int getTestsTotal() {
        return testsTotal;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
