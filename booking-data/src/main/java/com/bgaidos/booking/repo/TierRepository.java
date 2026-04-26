package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TierRepository extends JpaRepository<Tier, UUID> {

    @Query("""
        select t from Tier t
        where t.tenantId = :#{currentUser.tenantId()}
          and t.deletedAt is null
        """)
    List<Tier> findAllForCurrentTenant();

    @Query("""
        select t from Tier t
        where t.id = :id
          and t.tenantId = :#{currentUser.tenantId()}
          and t.deletedAt is null
        """)
    Optional<Tier> findByIdForCurrentTenant(@Param("id") UUID id);
}
