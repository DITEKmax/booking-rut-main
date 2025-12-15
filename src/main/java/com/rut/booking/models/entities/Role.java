package com.rut.booking.models.entities;

import com.rut.booking.models.enums.RoleType;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private RoleType code;

    @Column(name = "description")
    private String description;

    public Role() {
    }

    public Role(RoleType code) {
        this.code = code;
        this.description = code.getDisplayName();
    }

    public RoleType getCode() {
        return code;
    }

    public void setCode(RoleType code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
