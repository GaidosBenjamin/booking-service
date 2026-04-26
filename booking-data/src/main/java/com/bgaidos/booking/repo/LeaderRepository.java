package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Leader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaderRepository extends JpaRepository<Leader, UUID> {

    @Query("""
        select l from Leader l
        where l.tenantId = :#{currentUser.tenantId()}
        """)
    List<Leader> findAllForCurrentTenant();

    @Query("""
        select l from Leader l
        where l.id = :id
          and l.tenantId = :#{currentUser.tenantId()}
        """)
    Optional<Leader> findByIdForCurrentTenant(@Param("id") UUID id);
}
