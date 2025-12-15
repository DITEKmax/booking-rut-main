package com.rut.booking.models.enums;

public enum BookingStatus {
    CREATED("Created", "Application has been created"),
    PENDING("Pending", "Application is pending review"),
    APPROVED("Approved", "Application has been approved"),
    REJECTED("Rejected", "Application has been rejected"),
    CANCELLED("Cancelled", "Application has been cancelled");

    private final String displayName;
    private final String description;

    BookingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
