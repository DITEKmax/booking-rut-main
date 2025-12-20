package com.rut.booking.controllers;

import com.rut.booking.dto.BookingCreateRequest;
import com.rut.booking.dto.BookingDto;
import com.rut.booking.dto.RoomDto;
import com.rut.booking.models.enums.ClassPeriod;
import com.rut.booking.models.exceptions.BookingConflictException;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.BookingService;
import com.rut.booking.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;

    public BookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @GetMapping("/room/{roomId}")
    public String showBookingForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable Long roomId,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  Model model) {
        RoomDto room = roomService.getRoomById(roomId, userDetails.getUserId());
        List<RoomDto> similarRooms = roomService.getSimilarRooms(roomId, 5);

        LocalDate selectedDate = date != null ? date : LocalDate.now();
        List<ClassPeriod> availablePeriods = roomService.getAvailablePeriods(roomId, selectedDate);

        BookingCreateRequest bookingRequest = new BookingCreateRequest();
        bookingRequest.setRoomId(roomId);
        bookingRequest.setBookingDate(selectedDate);

        model.addAttribute("room", room);
        model.addAttribute("similarRooms", similarRooms);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availablePeriods", availablePeriods);
        model.addAttribute("allPeriods", ClassPeriod.values());
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("user", userDetails);

        return "pages/booking/form";
    }

    @PostMapping("/create")
    public String createBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("bookingRequest") BookingCreateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            RoomDto room = roomService.getRoomById(request.getRoomId(), userDetails.getUserId());
            List<ClassPeriod> availablePeriods = roomService.getAvailablePeriods(request.getRoomId(), request.getBookingDate());
            model.addAttribute("room", room);
            model.addAttribute("availablePeriods", availablePeriods);
            model.addAttribute("allPeriods", ClassPeriod.values());
            model.addAttribute("selectedDate", request.getBookingDate());
            model.addAttribute("user", userDetails);
            return "pages/booking/form";
        }

        try {
            BookingDto booking = bookingService.createBooking(userDetails.getUserId(), request);
            redirectAttributes.addFlashAttribute("success", "Booking created successfully!");
            redirectAttributes.addFlashAttribute("bookingId", booking.getId());
            return "redirect:/booking/confirmation/" + booking.getId();
        } catch (BookingConflictException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking/room/" + request.getRoomId();
        }
    }

    @GetMapping("/confirmation/{id}")
    public String bookingConfirmation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long id,
                                      Model model) {
        BookingDto booking = bookingService.getBookingById(id);

        // Check if user owns this booking
        if (!booking.getTeacherId().equals(userDetails.getUserId())) {
            return "redirect:/my-bookings";
        }

        RoomDto room = roomService.getRoomById(booking.getRoomId());
        List<RoomDto> similarRooms = roomService.getSimilarRooms(booking.getRoomId(), 5);

        model.addAttribute("booking", booking);
        model.addAttribute("room", room);
        model.addAttribute("similarRooms", similarRooms);
        model.addAttribute("user", userDetails);

        return "pages/booking/confirmation";
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long id) {
        BookingDto booking = bookingService.getBookingById(id);

        // Check if user owns this booking or is admin/dispatcher
        if (!booking.getTeacherId().equals(userDetails.getUserId()) &&
                !userDetails.isAdmin() && !userDetails.isDispatcher()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            byte[] pdfBytes = bookingService.getBookingPdf(id);
            String filename = "booking_" + id + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-bookings";
    }
}
