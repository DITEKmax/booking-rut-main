package com.rut.booking.controllers;

import com.rut.booking.dto.ReviewCreateRequest;
import com.rut.booking.dto.ReviewDto;
import com.rut.booking.dto.RoomDto;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.ReviewService;
import com.rut.booking.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final RoomService roomService;

    public ReviewController(ReviewService reviewService, RoomService roomService) {
        this.reviewService = reviewService;
        this.roomService = roomService;
    }

    @GetMapping("/room/{roomId}/new")
    public String showReviewForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long roomId,
                                 Model model) {
        // Check if user has already reviewed this room
        if (reviewService.hasUserReviewedRoom(userDetails.getUserId(), roomId)) {
            return "redirect:/rooms/" + roomId;
        }

        RoomDto room = roomService.getRoomById(roomId);

        ReviewCreateRequest reviewRequest = new ReviewCreateRequest();
        reviewRequest.setRoomId(roomId);

        model.addAttribute("room", room);
        model.addAttribute("reviewRequest", reviewRequest);
        model.addAttribute("user", userDetails);

        return "pages/reviews/form";
    }

    @PostMapping("/create")
    public String createReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @Valid @ModelAttribute("reviewRequest") ReviewCreateRequest request,
                               BindingResult bindingResult,
                               @RequestParam(required = false) MultipartFile image,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            RoomDto room = roomService.getRoomById(request.getRoomId());
            model.addAttribute("room", room);
            model.addAttribute("user", userDetails);
            return "pages/reviews/form";
        }

        try {
            reviewService.createReview(userDetails.getUserId(), request, image);
            redirectAttributes.addFlashAttribute("success", "Review saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/rooms/" + request.getRoomId();
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id,
                               Model model) {
        ReviewDto review = reviewService.getReviewById(id);

        // Check ownership
        if (!review.getUserId().equals(userDetails.getUserId())) {
            return "redirect:/my-reviews";
        }

        RoomDto room = roomService.getRoomById(review.getRoomId());

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setRoomId(review.getRoomId());
        request.setRating(review.getRating());
        request.setComment(review.getComment());

        model.addAttribute("review", review);
        model.addAttribute("room", room);
        model.addAttribute("reviewRequest", request);
        model.addAttribute("user", userDetails);

        return "pages/reviews/edit";
    }

    @PostMapping("/update/{id}")
    public String updateReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id,
                               @Valid @ModelAttribute("reviewRequest") ReviewCreateRequest request,
                               BindingResult bindingResult,
                               @RequestParam(required = false) MultipartFile image,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            ReviewDto review = reviewService.getReviewById(id);
            RoomDto room = roomService.getRoomById(review.getRoomId());
            model.addAttribute("review", review);
            model.addAttribute("room", room);
            model.addAttribute("user", userDetails);
            return "pages/reviews/edit";
        }

        try {
            reviewService.updateReview(id, userDetails.getUserId(), request, image);
            redirectAttributes.addFlashAttribute("success", "Review updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/my-reviews";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "/my-reviews") String redirectUrl,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }
}
