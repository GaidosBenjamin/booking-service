package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Booking;
import com.bgaidos.booking.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("""
        select b from Booking b
        where b.parentUser.id = :#{currentUser.userId()}
          and b.tenantId = :#{currentUser.tenantId()}
          and b.status in :statuses
        """)
    List<Booking> findAllForCurrentUser(@Param("statuses") List<PaymentStatus> statuses);

    @Query("""
        select b from Booking b
        where b.id = :id
          and b.parentUser.id = :#{currentUser.userId()}
          and b.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<Booking> findByIdForCurrentUser(@Param("id") UUID id);

    @Query("select b from Booking b where b.stripeSessionId = :sessionId")
    Optional<Booking> findByStripeSessionId(@Param("sessionId") String sessionId);

    @Query("""
        select count(b) from Booking b
        where b.parentUser.id = :#{currentUser.userId()}
          and b.tenantId = :#{currentUser.tenantId()}
          and b.status = :status
        """)
    long countByStatus(@Param("status") PaymentStatus status);

    @Modifying
    @Query("""
        update Booking b set b.status = 'CANCELED'
        where b.status = 'PENDING'
          and b.expiresAt < current_timestamp
        """)
    int cancelExpiredPending();

    @Modifying
    @Query("""
        update Booking b set b.status = :newStatus
        where b.stripeSessionId = :sessionId
          and b.status = :oldStatus
        """)
    int updateStatus(
        @Param("sessionId") String sessionId,
        @Param("oldStatus") PaymentStatus oldStatus,
        @Param("newStatus") PaymentStatus newStatus
    );
}
