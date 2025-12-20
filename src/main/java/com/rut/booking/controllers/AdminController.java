package com.rut.booking.controllers;

import com.rut.booking.dto.BookingDto;
import com.rut.booking.dto.CalendarEventDto;
import com.rut.booking.dto.ReviewDto;
import com.rut.booking.dto.RoomDto;
import com.rut.booking.models.enums.BookingStatus;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.BookingService;
import com.rut.booking.services.ReviewService;
import com.rut.booking.services.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final ReviewService reviewService;

    public AdminController(BookingService bookingService, RoomService roomService, ReviewService reviewService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long pendingCount = bookingService.countByStatus(BookingStatus.PENDING) +
                           bookingService.countByStatus(BookingStatus.CREATED);
        Long approvedCount = bookingService.countByStatus(BookingStatus.APPROVED);
        Long rejectedCount = bookingService.countByStatus(BookingStatus.REJECTED);

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("user", userDetails);

        return "admin/dashboard";
    }

    @GetMapping("/bookings")
    public String listBookings(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(required = false) BookingStatus status,
                               @RequestParam(required = false) Long roomId,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               Model model) {
        List<BookingDto> bookings;
        if (status != null || roomId != null || startDate != null || endDate != null) {
            bookings = bookingService.filterBookings(status, roomId, null, startDate, endDate);
        } else {
            bookings = bookingService.getAllBookings();
        }

        List<RoomDto> rooms = roomService.getAllActiveRooms();

        model.addAttribute("bookings", bookings);
        model.addAttribute("rooms", rooms);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("user", userDetails);

        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long id,
                                 Model model) {
        BookingDto booking = bookingService.getBookingById(id);
        RoomDto room = roomService.getRoomById(booking.getRoomId());

        model.addAttribute("booking", booking);
        model.addAttribute("room", room);
        model.addAttribute("user", userDetails);

        return "admin/booking-details";
    }

    @PostMapping("/bookings/{id}/reject")
    public String rejectBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Long id,
                                @RequestParam String reason,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.rejectBooking(id, reason, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("success", "Booking rejected successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/calendar")
    public String calendar(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(required = false) Long roomId,
                           Model model) {
        List<RoomDto> rooms = roomService.getAllActiveRooms();

        model.addAttribute("rooms", rooms);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("user", userDetails);

        return "admin/calendar";
    }

    @GetMapping("/api/calendar-events")
    @ResponseBody
    public ResponseEntity<List<CalendarEventDto>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long roomId) {
        List<CalendarEventDto> events;
        if (roomId != null) {
            events = bookingService.getRoomCalendarEvents(roomId, start, end);
        } else {
            events = bookingService.getCalendarEvents(start, end);
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/reviews")
    public String listReviews(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(required = false) Long roomId,
                              Model model) {
        List<ReviewDto> reviews;
        if (roomId != null) {
            reviews = reviewService.getReviewsByRoom(roomId);
        } else {
            reviews = reviewService.getAllReviews();
        }

        List<RoomDto> rooms = roomService.getAllActiveRooms();

        model.addAttribute("reviews", reviews);
        model.addAttribute("rooms", rooms);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("user", userDetails);

        return "admin/reviews";
    }

    // Delete review functionality removed - control room should not delete reviews
    // Reviews can only be deleted by the user who created them
    // @PostMapping("/reviews/{id}/delete")
    // public String deleteReview(...) { ... }

    @GetMapping("/dispatcher-issues")
    public String dispatcherIssues(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<ReviewDto> reviewsWithIssues = reviewService.getReviewsWithIssues();
        List<RoomDto> rooms = roomService.getAllActiveRooms();

        model.addAttribute("reviews", reviewsWithIssues);
        model.addAttribute("rooms", rooms);
        model.addAttribute("user", userDetails);

        return "admin/dispatcher-issues";
    }

    @GetMapping("/deleted-reviews")
    public String deletedReviews(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<ReviewDto> deletedReviews = reviewService.getDeletedReviews();
        List<RoomDto> rooms = roomService.getAllActiveRooms();

        model.addAttribute("reviews", deletedReviews);
        model.addAttribute("rooms", rooms);
        model.addAttribute("user", userDetails);

        return "admin/deleted-reviews";
    }

    @GetMapping("/api/reviews")
    @ResponseBody
    public ResponseEntity<List<ReviewDto>> getAllReviews(@RequestParam(required = false) Long roomId) {
        List<ReviewDto> reviews;
        if (roomId != null) {
            reviews = reviewService.getReviewsByRoom(roomId);
        } else {
            reviews = reviewService.getAllReviews();
        }
        return ResponseEntity.ok(reviews);
    }
}
