package com.rut.booking.models.enums;

import java.time.LocalTime;

public enum ClassPeriod {
    PERIOD_1(1, LocalTime.of(8, 30), LocalTime.of(9, 50), "1st Period"),
    PERIOD_2(2, LocalTime.of(10, 5), LocalTime.of(11, 25), "2nd Period"),
    PERIOD_3(3, LocalTime.of(11, 40), LocalTime.of(13, 0), "3rd Period"),
    PERIOD_4(4, LocalTime.of(13, 45), LocalTime.of(15, 5), "4th Period"),
    PERIOD_5(5, LocalTime.of(15, 20), LocalTime.of(16, 40), "5th Period"),
    PERIOD_6(6, LocalTime.of(16, 55), LocalTime.of(18, 15), "6th Period"),
    PERIOD_7(7, LocalTime.of(18, 30), LocalTime.of(19, 50), "7th Period"),
    PERIOD_8(8, LocalTime.of(20, 0), LocalTime.of(21, 20), "8th Period");

    private final int number;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String displayName;

    ClassPeriod(int number, LocalTime startTime, LocalTime endTime, String displayName) {
        this.number = number;
        this.startTime = startTime;
        this.endTime = endTime;
        this.displayName = displayName;
    }

    public int getNumber() {
        return number;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTimeRange() {
        return String.format("%s – %s",
            startTime.toString(),
            endTime.toString());
    }

    public static ClassPeriod fromNumber(int number) {
        for (ClassPeriod period : values()) {
            if (period.number == number) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid class period number: " + number);
    }
}
