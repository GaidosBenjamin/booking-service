package com.bgaidos.booking.data;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditEntity extends EntityBase {

    @CreatedDate
    private OffsetDateTime createdOn;

    @CreatedBy
    private UUID createdBy;

    @LastModifiedDate
    private OffsetDateTime modifiedOn;

    @LastModifiedBy
    private UUID modifiedBy;
}
