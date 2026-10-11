package com.lrch.javalab.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Ultimo codigo guardado por el usuario para un ejercicio.
 *
 * <p>Permite retomar un ejercicio donde se dejo. Una vez que el ejercicio se resuelve,
 * {@code solved} se queda en true aunque despues se envie una version que falle.
 */
@Entity
@Table(name = "user_solutions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exercise_slug"}))
public class UserSolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exercise_slug", nullable = false)
    private String exerciseSlug;

    @Column(name = "code", length = 20_000)
    private String code;

    @Column(name = "solved", nullable = false)
    private boolean solved;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "updated_at", nullable = false)
    private long updatedAt;

    protected UserSolution() {
    }

    public UserSolution(Long userId, String exerciseSlug) {
        this.userId = userId;
        this.exerciseSlug = exerciseSlug;
        this.solved = false;
        this.attempts = 0;
        this.updatedAt = System.currentTimeMillis();
    }

    public void registrarEnvio(String code, boolean aprobado) {
        this.code = code;
        this.attempts++;
        this.solved = this.solved || aprobado;
        this.updatedAt = System.currentTimeMillis();
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

    public String getCode() {
        return code;
    }

    public boolean isSolved() {
        return solved;
    }

    public int getAttempts() {
        return attempts;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
}
