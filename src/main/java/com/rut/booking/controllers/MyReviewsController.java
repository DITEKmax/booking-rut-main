package com.rut.booking.controllers;

import com.rut.booking.dto.ReviewDto;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/my-reviews")
public class MyReviewsController {

    private final ReviewService reviewService;

    public MyReviewsController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public String myReviews(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) Integer rating,
                           @RequestParam(required = false) String sort,
                           @RequestParam(required = false, defaultValue = "false") Boolean withPhotos,
                           Model model) {
        List<ReviewDto> reviews = reviewService.getReviewsByUser(userDetails.getUserId());

        // Apply text search
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            reviews = reviews.stream()
                    .filter(r -> r.getComment() != null && r.getComment().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        // Filter by rating
        if (rating != null && rating >= 1 && rating <= 5) {
            reviews = reviews.stream()
                    .filter(r -> r.getRating() == rating)
                    .collect(Collectors.toList());
        }

        // Filter by photos
        if (withPhotos) {
            reviews = reviews.stream()
                    .filter(r -> r.getImagePath() != null && !r.getImagePath().isEmpty())
                    .collect(Collectors.toList());
        }

        // Sort by novelty (newest first or oldest first)
        if ("newest".equals(sort)) {
            reviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        } else if ("oldest".equals(sort)) {
            reviews.sort((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));
        } else {
            // Default: newest first
            reviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("user", userDetails);
        model.addAttribute("search", search);
        model.addAttribute("rating", rating);
        model.addAttribute("sort", sort);
        model.addAttribute("withPhotos", withPhotos);

        return "pages/my-reviews";
    }
}
