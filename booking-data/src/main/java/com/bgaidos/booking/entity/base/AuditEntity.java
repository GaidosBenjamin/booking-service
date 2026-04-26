package com.bgaidos.booking.entity.base;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditEntity extends EntityBase {

    @CreatedDate
    private Instant createdOn;

    @CreatedBy
    private UUID createdBy;

    @LastModifiedDate
    private Instant modifiedOn;

    @LastModifiedBy
    private UUID modifiedBy;
}
