package com.rut.booking.controllers;

import com.rut.booking.dto.ReviewDto;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/my-reviews")
public class MyReviewsController {

    private final ReviewService reviewService;

    public MyReviewsController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public String myReviews(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<ReviewDto> reviews = reviewService.getReviewsByUser(userDetails.getUserId());
        model.addAttribute("reviews", reviews);
        model.addAttribute("user", userDetails);
        return "pages/my-reviews";
    }
}
