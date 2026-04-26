package com.bgaidos.booking.entity;

import com.bgaidos.booking.entity.base.EntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "members")
@Getter
@Setter
public class Member extends EntityBase {

    private UUID tenantId;
    private String name;
    private String email;
    private String phone;
}
