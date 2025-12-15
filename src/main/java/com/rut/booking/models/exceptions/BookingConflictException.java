package com.rut.booking.models.exceptions;

public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }

    public BookingConflictException(String roomNumber, String date, String time) {
        super(String.format("Room %s is already booked on %s at %s", roomNumber, date, time));
    }
}
