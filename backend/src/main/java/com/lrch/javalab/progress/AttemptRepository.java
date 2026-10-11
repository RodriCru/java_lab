package com.lrch.javalab.progress;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    @Query("select distinct a.exerciseSlug from Attempt a where a.userId = ?1")
    List<String> findSlugsIntentados(Long userId);

    @Query("select distinct a.exerciseSlug from Attempt a where a.userId = ?1 and a.passed = true")
    List<String> findSlugsResueltos(Long userId);

    @Query("select a.createdAt from Attempt a where a.userId = ?1 and a.passed = true order by a.createdAt desc")
    List<Long> findInstantesResueltos(Long userId);
}
