package com.bgaidos.booking.data.repo;

import com.bgaidos.booking.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("select r from Role r where r.tenantId = :tenantId and r.name = :name")
    Optional<Role> findByTenantIdAndName(@Param("tenantId") UUID tenantId, @Param("name") String name);
}
