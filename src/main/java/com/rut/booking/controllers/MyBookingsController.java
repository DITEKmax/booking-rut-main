package com.rut.booking.controllers;

import com.rut.booking.dto.BookingDto;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.BookingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/my-bookings")
public class MyBookingsController {

    private final BookingService bookingService;

    public MyBookingsController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String myBookings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<BookingDto> bookings = bookingService.getBookingsByTeacher(userDetails.getUserId());
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", userDetails);
        return "pages/my-bookings";
    }
}
