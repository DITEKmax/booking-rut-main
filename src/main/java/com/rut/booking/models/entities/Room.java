package com.rut.booking.models.entities;

import com.rut.booking.models.enums.RoomType;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room extends BaseEntity {

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(name = "building", length = 10)
    private String building;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "capacity", nullable = false)
    private Integer capacity = 0;

    @Column(name = "has_computers")
    private Boolean hasComputers = false;

    @Column(name = "has_projector")
    private Boolean hasProjector = false;

    @Column(name = "has_whiteboard")
    private Boolean hasWhiteboard = false;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    public Room() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
        parseRoomNumber(number);
    }

    private void parseRoomNumber(String number) {
        if (number != null && number.length() >= 4) {
            this.building = number.substring(0, 1);
            this.floor = Integer.parseInt(number.substring(1, 2));
        }
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getHasComputers() {
        return hasComputers;
    }

    public void setHasComputers(Boolean hasComputers) {
        this.hasComputers = hasComputers;
    }

    public Boolean getHasProjector() {
        return hasProjector;
    }

    public void setHasProjector(Boolean hasProjector) {
        this.hasProjector = hasProjector;
    }

    public Boolean getHasWhiteboard() {
        return hasWhiteboard;
    }

    public void setHasWhiteboard(Boolean hasWhiteboard) {
        this.hasWhiteboard = hasWhiteboard;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }

    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public String getDisplayName() {
        return "Room " + number + " (" + roomType.getDisplayName() + ")";
    }
}
