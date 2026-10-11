package com.lrch.javalab.questions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Una respuesta dada a una pregunta del banco. Es historial: nunca se borra. */
@Entity
@Table(name = "question_attempts")
public class QuestionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "selected_index", nullable = false)
    private int selectedIndex;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    protected QuestionAttempt() {
    }

    public QuestionAttempt(Long userId, String questionId, int selectedIndex, boolean correct) {
        this.userId = userId;
        this.questionId = questionId;
        this.selectedIndex = selectedIndex;
        this.correct = correct;
        this.createdAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean isCorrect() {
        return correct;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
