package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    @Query("select t from EmailVerificationToken t where t.user.id = :userId and t.tokenHash = :tokenHash and t.consumedAt is null")
    Optional<EmailVerificationToken> findActiveByUserIdAndTokenHash(@Param("userId") UUID userId, @Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update EmailVerificationToken t set t.consumedAt = :consumedAt where t.user.id = :userId and t.consumedAt is null")
    int invalidateAllForUser(@Param("userId") UUID userId, @Param("consumedAt") Instant consumedAt);
}
