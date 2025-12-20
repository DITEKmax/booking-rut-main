package com.rut.booking.dto;

import com.rut.booking.models.entities.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    public UserDto toUserDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLastName(user.getLastName());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setPhone(user.getPhone());
        dto.setRoleType(user.getRole() != null ? user.getRole().getCode() : null);
        dto.setFullName(user.getFullName());
        dto.setShortName(user.getShortName());
        dto.setIsActive(user.getIsActive());
        return dto;
    }

    public RoomDto toRoomDto(Room room) {
        if (room == null) return null;
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setNumber(room.getNumber());
        dto.setRoomType(room.getRoomType());
        dto.setBuilding(room.getBuilding());
        dto.setFloor(room.getFloor());
        dto.setCapacity(room.getCapacity());
        dto.setHasComputers(room.getHasComputers());
        dto.setHasProjector(room.getHasProjector());
        dto.setHasWhiteboard(room.getHasWhiteboard());
        dto.setDescription(room.getDescription());
        dto.setImagePath(room.getImagePath());
        dto.setAverageRating(room.getAverageRating());
        dto.setReviewCount(room.getReviews() != null ? room.getReviews().size() : 0);
        return dto;
    }

    public RoomDto toRoomDto(Room room, boolean isFavorite) {
        RoomDto dto = toRoomDto(room);
        if (dto != null) {
            dto.setIsFavorite(isFavorite);
        }
        return dto;
    }

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) return null;
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setTeacherId(booking.getTeacher() != null ? booking.getTeacher().getId() : null);
        dto.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getFullName() : null);
        dto.setRoomId(booking.getRoom() != null ? booking.getRoom().getId() : null);
        dto.setRoomNumber(booking.getRoom() != null ? booking.getRoom().getNumber() : null);
        dto.setRoomDisplayName(booking.getRoom() != null ? booking.getRoom().getDisplayName() : null);
        dto.setBookingDate(booking.getBookingDate());
        dto.setClassPeriod(booking.getClassPeriod());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setPurpose(booking.getPurpose());
        dto.setNotes(booking.getNotes());
        dto.setStatus(booking.getStatus());
        dto.setRejectionReason(booking.getRejectionReason());
        dto.setPdfFilePath(booking.getPdfFilePath());
        dto.setPdfGeneratedAt(booking.getPdfGeneratedAt());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }

    public ReviewDto toReviewDto(Review review) {
        if (review == null) return null;
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUser() != null ? review.getUser().getId() : null);
        dto.setUserName(review.getUser() != null ? review.getUser().getFullName() : null);
        dto.setRoomId(review.getRoom() != null ? review.getRoom().getId() : null);
        dto.setRoomNumber(review.getRoom() != null ? review.getRoom().getNumber() : null);
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setImagePath(review.getImagePath());
        dto.setIssues(review.getIssues());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    public CalendarEventDto toCalendarEventDto(Booking booking) {
        if (booking == null) return null;
        CalendarEventDto dto = new CalendarEventDto();
        dto.setBookingId(booking.getId());
        dto.setRoomNumber(booking.getRoom() != null ? booking.getRoom().getNumber() : null);
        dto.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getShortName() : null);
        dto.setDate(booking.getBookingDate());
        dto.setPeriod(booking.getClassPeriod() != null ? booking.getClassPeriod().getNumber() : null);
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setPurpose(booking.getPurpose());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
