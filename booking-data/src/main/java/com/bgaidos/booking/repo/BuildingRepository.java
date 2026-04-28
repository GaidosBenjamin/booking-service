package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {

    @Query("""
        select b from Building b
        where b.tenantId = :#{currentUser.tenantId()}
        order by b.tier.basePrice desc
        """)
    List<Building> findAllForCurrentTenant();

    @Query("""
        select b from Building b
        where b.tenantId = :#{currentUser.tenantId()}
          and exists (
            select r from Room r
            where r.building = b
              and r.leaderRoom = false
              and (r.allowedGender is null or r.allowedGender = :gender)
              and (r.minAge is null or :age >= r.minAge)
              and (r.maxAge is null or :age <= r.maxAge)
          )
        order by b.tier.basePrice desc
        """)
    List<Building> findForCurrentTenantFiltered(
        @Param("gender") String gender,
        @Param("age") int age,
        @Param("now") Instant now
    );

    @Query("""
        select count(r) > 0 from Room r
        where r.building.id = :buildingId
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
        @Param("buildingId") UUID buildingId,
        @Param("gender") String gender,
        @Param("age") int age,
        @Param("now") Instant now
    );

    @Query("""
        select b from Building b
        where b.id = :id
          and b.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<Building> findByIdForCurrentTenant(@Param("id") UUID id);
}
