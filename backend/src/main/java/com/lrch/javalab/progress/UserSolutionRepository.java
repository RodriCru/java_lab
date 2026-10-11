package com.lrch.javalab.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSolutionRepository extends JpaRepository<UserSolution, Long> {

    Optional<UserSolution> findByUserIdAndExerciseSlug(Long userId, String exerciseSlug);

    List<UserSolution> findByUserId(Long userId);
}
