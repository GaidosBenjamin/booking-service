package com.bgaidos.booking.entity;

import com.bgaidos.booking.entity.base.EntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "code_of_conduct_agreements")
@Getter
@Setter
public class CodeOfConductAgreement extends EntityBase {

    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_of_conduct_id")
    private CodeOfConduct codeOfConduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Instant agreedOn;
}
