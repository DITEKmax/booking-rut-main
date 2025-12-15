package com.rut.booking.models.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s with id %d not found", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(String.format("%s with %s '%s' not found", resourceName, field, value));
    }
}
