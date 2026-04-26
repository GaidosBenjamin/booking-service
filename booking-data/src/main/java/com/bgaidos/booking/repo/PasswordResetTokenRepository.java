package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Query("select t from PasswordResetToken t where t.user.id = :userId and t.tokenHash = :tokenHash and t.consumedAt is null")
    Optional<PasswordResetToken> findActiveByUserIdAndTokenHash(@Param("userId") UUID userId, @Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update PasswordResetToken t set t.consumedAt = :consumedAt where t.user.id = :userId and t.consumedAt is null")
    int invalidateAllForUser(@Param("userId") UUID userId, @Param("consumedAt") Instant consumedAt);
}
