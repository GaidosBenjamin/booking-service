package com.bgaidos.booking.entity;

import com.bgaidos.booking.entity.base.AuditEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tiers")
@Getter
@Setter
public class Tier extends AuditEntity {

    private UUID tenantId;

    private String name;
    private String description;

    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private String currency;

    private Instant deletedAt;
}
