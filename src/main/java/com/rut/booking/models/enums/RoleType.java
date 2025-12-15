package com.rut.booking.models.enums;

public enum RoleType {
    TEACHER("Teacher"),
    DISPATCHER("Dispatcher"),
    ADMIN("Administrator");

    private final String displayName;

    RoleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
