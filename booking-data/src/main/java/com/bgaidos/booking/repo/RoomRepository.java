package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    @Query("""
        select r from Room r
        where r.tenantId = :#{currentUser.tenantId()}
        order by r.name
        """)
    List<Room> findAllForCurrentTenant();

    @Query("""
        select r from Room r
        where r.tenantId = :#{currentUser.tenantId()}
          and (r.allowedGender is null or r.allowedGender = :gender)
          and (r.minAge is null or :age >= r.minAge)
          and (r.maxAge is null or :age <= r.maxAge)
          and (:buildingId is null or r.building.id = :buildingId)
        order by r.name
        """)
    List<Room> findForCurrentTenantFiltered(
        @Param("gender") String gender,
        @Param("age") int age,
        @Param("buildingId") UUID buildingId);

    @Query("""
        select r from Room r
        where r.id = :id
          and r.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<Room> findByIdForCurrentTenant(@Param("id") UUID id);

    @Query("""
        select count(r) > 0 from Room r
        where r.tenantId = :#{currentUser.tenantId()}
          and r.leaderRoom = false
          and (r.allowedGender is null or r.allowedGender = :gender)
          and (r.minAge is null or :age >= r.minAge)
          and (r.maxAge is null or :age <= r.maxAge)
          and r.capacity > (
            select count(a) from RoomAssignment a where a.room = r
          ) + (
            select count(h) from RoomHold h where h.room = r and h.expiresAt > :now
          )
        """)
    boolean hasAvailableRooms(
        @Param("gender") String gender,
        @Param("age") int age,
        @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r from Room r
        where r.id = :id
          and r.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<Room> findByIdForCurrentTenantForUpdate(@Param("id") UUID id);
}
