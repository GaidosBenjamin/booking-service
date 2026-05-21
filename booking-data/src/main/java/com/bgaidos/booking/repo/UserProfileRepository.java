package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    @Query("""
        select p from UserProfile p
        where p.tenantId = :tenantId
        and exists (
            select 1 from Camper c where c.parentUser = p.user and c.tenantId = :tenantId
        )
        """)
    List<UserProfile> findProfilesOfParentsWithCampers(@Param("tenantId") UUID tenantId);
}
