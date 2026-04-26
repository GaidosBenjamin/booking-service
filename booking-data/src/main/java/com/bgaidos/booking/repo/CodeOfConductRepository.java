package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.CodeOfConduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CodeOfConductRepository extends JpaRepository<CodeOfConduct, UUID> {

    @Query("""
        select c from CodeOfConduct c
        where c.tenantId = :#{currentUser.tenantId()}
        """)
    List<CodeOfConduct> findAllForCurrentTenant();

    @Query("""
        select c from CodeOfConduct c
        where c.id = :id
          and c.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<CodeOfConduct> findByIdForCurrentTenant(@Param("id") UUID id);

    @Modifying
    @Query("""
        update CodeOfConduct c
        set c.active = false
        where c.tenantId = :#{currentUser.tenantId()}
          and c.active = true
        """)
    void deactivateAllActiveForCurrentTenant();
}
