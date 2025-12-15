package com.rut.booking.dto;

import com.rut.booking.models.enums.BookingStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarEventDto {
    private Long bookingId;
    private String roomNumber;
    private String teacherName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private BookingStatus status;
    private String color;

    public CalendarEventDto() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
        this.color = getColorForStatus(status);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private String getColorForStatus(BookingStatus status) {
        return switch (status) {
            case APPROVED -> "#28a745";
            case PENDING, CREATED -> "#ffc107";
            case REJECTED -> "#dc3545";
            case CANCELLED -> "#6c757d";
        };
    }

    public String getTimeRange() {
        return String.format("%s – %s", startTime.toString(), endTime.toString());
    }
}
