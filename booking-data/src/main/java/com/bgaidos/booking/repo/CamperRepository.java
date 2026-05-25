package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Camper;
import com.bgaidos.booking.entity.CamperStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CamperRepository extends JpaRepository<Camper, UUID> {

    @Query("""
        select c from Camper c
        where c.tenantId = :#{currentUser.tenantId()}
          and c.parentUser.id = :#{currentUser.userId()}
        """)
    List<Camper> findAllForCurrentUser();

    @Query("""
        select c from Camper c
        where c.id = :id
          and c.tenantId = :#{currentUser.tenantId()}
          and c.parentUser.id = :#{currentUser.userId()}
        """)
    Optional<Camper> findByIdForCurrentUser(@Param("id") UUID id);

    @Query("""
        select c from Camper c
        where c.id = :id
          and c.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<Camper> findByIdForCurrentTenant(@Param("id") UUID id);

    @Query("""
        select c from Camper c
        where c.parentUser.id = :parentUserId
          and c.tenantId = :tenantId
          and c.status in :statuses
        """)
    List<Camper> findByParentAndStatuses(
        @Param("parentUserId") UUID parentUserId,
        @Param("tenantId") UUID tenantId,
        @Param("statuses") List<CamperStatus> statuses);
}
