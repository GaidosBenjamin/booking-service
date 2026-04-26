package com.bgaidos.booking.entity;

import com.bgaidos.booking.entity.base.AuditEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Organization extends AuditEntity {

    private String slug;
    private String name;
}
