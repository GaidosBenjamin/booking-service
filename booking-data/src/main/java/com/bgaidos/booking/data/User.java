package com.bgaidos.booking.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends AuditEntity {

    private UUID tenantId;
    private String email;
    private String passwordHash;
}
