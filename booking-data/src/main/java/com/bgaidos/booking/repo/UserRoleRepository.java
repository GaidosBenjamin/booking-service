package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Role;
import com.bgaidos.booking.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    @Query("select ur.role from UserRole ur where ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") UUID userId);
}
