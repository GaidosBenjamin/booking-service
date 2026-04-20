package com.bgaidos.booking.data.entity;

import com.bgaidos.booking.data.entity.base.AuditEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
public class UserProfile extends AuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private UUID tenantId;

    private String firstName;
    private String lastName;
    private String address;
}
