package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where u.tenantId = :tenantId and lower(u.email) = lower(:email)")
    Optional<User> findByTenantIdAndEmailIgnoreCase(@Param("tenantId") UUID tenantId, @Param("email") String email);

    @Query("select count(u) > 0 from User u where u.tenantId = :tenantId and lower(u.email) = lower(:email)")
    boolean existsByTenantIdAndEmailIgnoreCase(@Param("tenantId") UUID tenantId, @Param("email") String email);

    @Query("select count(u) from User u where u.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query("select u from User u where u.id = :id and u.tenantId = :tenantId")
    Optional<User> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
