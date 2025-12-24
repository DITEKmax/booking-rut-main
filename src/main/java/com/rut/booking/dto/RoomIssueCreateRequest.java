package com.rut.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoomIssueCreateRequest {

    @NotNull(message = "Выберите аудиторию")
    private Long roomId;

    @NotBlank(message = "Выберите хотя бы одну проблему")
    private String issues;

    private String description;

    public RoomIssueCreateRequest() {
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
