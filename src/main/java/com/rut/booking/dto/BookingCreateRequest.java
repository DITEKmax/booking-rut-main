package com.rut.booking.dto;

import com.rut.booking.models.enums.ClassPeriod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class BookingCreateRequest {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date must be today or in the future")
    private LocalDate bookingDate;

    @NotNull(message = "Class period is required")
    private ClassPeriod classPeriod;

    @NotNull(message = "Purpose is required")
    @Size(min = 5, max = 500, message = "Purpose must be between 5 and 500 characters")
    private String purpose;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    public BookingCreateRequest() {
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public ClassPeriod getClassPeriod() {
        return classPeriod;
    }

    public void setClassPeriod(ClassPeriod classPeriod) {
        this.classPeriod = classPeriod;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
