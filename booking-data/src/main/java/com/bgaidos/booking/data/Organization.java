package com.bgaidos.booking.data;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Organization extends AuditEntity {

    private String name;
}
