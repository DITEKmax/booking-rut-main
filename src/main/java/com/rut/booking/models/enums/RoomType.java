package com.rut.booking.models.enums;

public enum RoomType {
    LECTURE("Lecture Hall", "Large lecture auditorium"),
    COMPUTER("Computer Lab", "Computer laboratory with workstations"),
    LAB("Laboratory", "Science or engineering laboratory"),
    SEMINAR("Seminar Room", "Small seminar or discussion room"),
    CONFERENCE("Conference Room", "Meeting and conference room");

    private final String displayName;
    private final String description;

    RoomType(String displayName, String description) {
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
