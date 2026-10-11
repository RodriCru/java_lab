package com.lrch.javalab.questions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

    @Query("select distinct a.questionId from QuestionAttempt a where a.userId = ?1")
    List<String> findIdsRespondidas(Long userId);

    @Query("select distinct a.questionId from QuestionAttempt a where a.userId = ?1 and a.correct = true")
    List<String> findIdsAcertadas(Long userId);
}
