package com.lrch.javalab.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

    List<EmailCode> findByUserIdAndPurposeAndUsedAtIsNull(Long userId, CodePurpose purpose);

    Optional<EmailCode> findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(Long userId, CodePurpose purpose);

    Optional<EmailCode> findFirstByUserIdAndPurposeOrderByCreatedAtDesc(Long userId, CodePurpose purpose);

    long countByUserIdAndPurposeAndCreatedAtGreaterThan(Long userId, CodePurpose purpose, long desde);
}
