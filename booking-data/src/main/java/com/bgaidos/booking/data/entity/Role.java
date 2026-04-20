package com.bgaidos.booking.data.entity;

import com.bgaidos.booking.data.entity.base.AuditEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends AuditEntity {

    private UUID tenantId;

    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> permissions;
}
