package com.bgaidos.booking.entity;

import com.bgaidos.booking.entity.base.EntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class EmailVerificationToken extends EntityBase {

    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String tokenHash;
    private Instant expiresAt;
    private Instant consumedAt;

    @CreatedDate
    private Instant createdOn;

    @CreatedBy
    private UUID createdBy;
}
