package com.bgaidos.booking.data.repo;

import com.bgaidos.booking.data.entity.Camper;
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
}
