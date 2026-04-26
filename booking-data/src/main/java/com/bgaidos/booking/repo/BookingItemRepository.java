package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.BookingItem;
import com.bgaidos.booking.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {

    @Query("select i from BookingItem i where i.booking.id = :bookingId")
    List<BookingItem> findAllByBookingId(@Param("bookingId") UUID bookingId);

    @Query("""
        select count(i) from BookingItem i
        where i.camper.id = :camperId
          and i.booking.status = :status
        """)
    long countByCamperIdAndBookingStatus(@Param("camperId") UUID camperId, @Param("status") PaymentStatus status);

    @Modifying
    @Query(
        value = """
            delete from booking_items
            where camper_id = :camperId
              and booking_id in (
                select id from bookings where status in ('FAILED', 'CANCELED')
              )
            """,
        nativeQuery = true)
    void deleteDeadItemsByCamperId(@Param("camperId") UUID camperId);
}
