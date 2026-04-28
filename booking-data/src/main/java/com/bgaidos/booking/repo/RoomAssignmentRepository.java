package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.RoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, UUID> {

    @Query("""
        select a from RoomAssignment a
        where a.tenantId = :#{currentUser.tenantId()}
        """)
    List<RoomAssignment> findAllForCurrentTenant();

    @Query("""
        select a from RoomAssignment a
        where a.id = :id
          and a.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<RoomAssignment> findByIdForCurrentTenant(@Param("id") UUID id);

    @Query("""
        select a from RoomAssignment a
        join fetch a.room r
        join fetch r.building
        where a.camper.id = :camperId
        """)
    Optional<RoomAssignment> findByCamperId(@Param("camperId") UUID camperId);

    @Query("""
        select a from RoomAssignment a
        join fetch a.room r
        join fetch r.building
        where a.camper.id in :camperIds
        """)
    List<RoomAssignment> findByCamperIds(@Param("camperIds") Collection<UUID> camperIds);

    @Query("select count(a) from RoomAssignment a where a.room.id = :roomId")
    long countByRoomId(@Param("roomId") UUID roomId);

    @Query("""
        select a from RoomAssignment a
        where a.tenantId = :#{currentUser.tenantId()}
          and a.room.id in :roomIds
        """)
    List<RoomAssignment> findByRoomIds(@Param("roomIds") Collection<UUID> roomIds);
}
