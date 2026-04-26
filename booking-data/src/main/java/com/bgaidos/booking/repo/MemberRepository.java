package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    @Query("""
        select count(m) > 0 from Member m
        where m.tenantId = :tenantId
          and (
            (:email is not null and lower(m.email) = :email)
            or (:phone is not null and lower(m.phone) = :phone)
          )
        """)
    boolean existsByTenantIdAndEmailOrPhone(
        @Param("tenantId") UUID tenantId,
        @Param("email") String email,
        @Param("phone") String phone
    );
}
