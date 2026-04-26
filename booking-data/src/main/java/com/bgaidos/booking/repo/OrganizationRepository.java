package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    @Query("select o from Organization o where o.slug = :slug")
    Optional<Organization> findBySlug(@Param("slug") String slug);

    @Query("select count(o) > 0 from Organization o where o.slug = :slug")
    boolean existsBySlug(@Param("slug") String slug);
}
