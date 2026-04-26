package com.bgaidos.booking.repo;

import com.bgaidos.booking.entity.CodeOfConductAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CodeOfConductAgreementRepository extends JpaRepository<CodeOfConductAgreement, UUID> {

    @Query("""
        select a from CodeOfConductAgreement a
        where a.user.id = :userId
          and a.codeOfConduct.id = :codeOfConductId
        """)
    Optional<CodeOfConductAgreement> findByUserIdAndCodeOfConductId(
        @Param("userId") UUID userId,
        @Param("codeOfConductId") UUID codeOfConductId);

    @Query("""
        select a from CodeOfConductAgreement a
        where a.user.id = :#{currentUser.userId()}
          and a.tenantId = :#{currentUser.tenantId()}
        """)
    List<CodeOfConductAgreement> findAllForCurrentUser();
}
