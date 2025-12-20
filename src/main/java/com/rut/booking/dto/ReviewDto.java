package com.rut.booking.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long roomId;
    private String roomNumber;
    private Integer rating;
    private String comment;
    private String imagePath;
    private String issues;
    private Boolean issuesMarkedRelevant;
    private LocalDateTime issuesMarkedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public Boolean getIssuesMarkedRelevant() {
        return issuesMarkedRelevant;
    }

    public void setIssuesMarkedRelevant(Boolean issuesMarkedRelevant) {
        this.issuesMarkedRelevant = issuesMarkedRelevant;
    }

    public LocalDateTime getIssuesMarkedAt() {
        return issuesMarkedAt;
    }

    public void setIssuesMarkedAt(LocalDateTime issuesMarkedAt) {
        this.issuesMarkedAt = issuesMarkedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
