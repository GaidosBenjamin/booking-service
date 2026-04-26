package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.RoomHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomHoldRepository extends JpaRepository<RoomHold, UUID> {

    @Query("""
        select h from RoomHold h
        where h.tenantId = :#{currentUser.tenantId()}
          and h.camper.parentUser.id = :#{currentUser.userId()}
          and h.expiresAt > :now
        """)
    List<RoomHold> findActiveForCurrentUser(@Param("now") Instant now);

    @Query("""
        select h from RoomHold h
        where h.id = :id
          and h.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<RoomHold> findByIdForCurrentTenant(@Param("id") UUID id);

    @Query("""
        select h from RoomHold h
        where h.id = :id
          and h.tenantId = :#{currentUser.tenantId()}
          and h.camper.parentUser.id = :#{currentUser.userId()}
        """)
    Optional<RoomHold> findByIdForCurrentUser(@Param("id") UUID id);

    @Query("select h from RoomHold h where h.camper.id = :camperId and h.expiresAt > current_timestamp")
    Optional<RoomHold> findByCamperId(@Param("camperId") UUID camperId);

    @Query("""
        select count(h) from RoomHold h
        where h.camper.id = :camperId
          and h.expiresAt > :now
        """)
    long countActiveByCamperId(@Param("camperId") UUID camperId, @Param("now") Instant now);

    @Query("""
        select count(h) from RoomHold h
        where h.room.id = :roomId
          and h.expiresAt > :now
        """)
    long countActiveByRoomId(@Param("roomId") UUID roomId, @Param("now") Instant now);

    @Query("""
        select h from RoomHold h
        where h.tenantId = :#{currentUser.tenantId()}
          and h.room.id in :roomIds
          and h.expiresAt > :now
        """)
    List<RoomHold> findActiveByRoomIds(@Param("roomIds") Collection<UUID> roomIds, @Param("now") Instant now);

    @Modifying
    @Query("""
        update RoomHold h set h.expiresAt = :expiresAt
        where h.camper.id in :camperIds
          and h.tenantId = :tenantId
        """)
    void extendByCamperIds(
        @Param("camperIds") Collection<UUID> camperIds,
        @Param("tenantId") UUID tenantId,
        @Param("expiresAt") Instant expiresAt);

    @Modifying
    @Query("""
        update RoomHold h set h.expiresAt = :expiresAt
        where h.camper.id in :camperIds
          and h.tenantId = :tenantId
          and h.expiresAt > current_timestamp
        """)
    void resetActiveByCamperIds(
        @Param("camperIds") Collection<UUID> camperIds,
        @Param("tenantId") UUID tenantId,
        @Param("expiresAt") Instant expiresAt);

    @Modifying
    @Query("delete from RoomHold h where h.camper.id = :camperId and h.tenantId = :tenantId")
    void deleteByCamperId(@Param("camperId") UUID camperId, @Param("tenantId") UUID tenantId);

    @Modifying
    @Query("delete from RoomHold h where h.expiresAt < current_timestamp")
    int deleteExpired();
}
