package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Donation;
import com.bgaidos.booking.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

    @Query("select d from Donation d where d.stripeSessionId = :sessionId")
    Optional<Donation> findByStripeSessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Query("""
        update Donation d set d.status = :newStatus
        where d.stripeSessionId = :sessionId
          and d.status = :oldStatus
        """)
    int updateStatus(
        @Param("sessionId") String sessionId,
        @Param("oldStatus") PaymentStatus oldStatus,
        @Param("newStatus") PaymentStatus newStatus
    );
}
