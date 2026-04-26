package com.bgaidos.booking.entity;

import com.bgaidos.booking.entity.base.AuditEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leaders")
@Getter
@Setter
public class Leader extends AuditEntity {

    private UUID tenantId;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
}
