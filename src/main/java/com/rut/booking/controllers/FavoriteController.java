package com.rut.booking.controllers;

import com.rut.booking.dto.RoomDto;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public String favorites(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<RoomDto> favoriteRooms = favoriteService.getUserFavorites(userDetails.getUserId());
        model.addAttribute("rooms", favoriteRooms);
        model.addAttribute("user", userDetails);
        return "pages/favorites";
    }

    @PostMapping("/toggle/{roomId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId) {
        boolean isFavorite = favoriteService.toggleFavorite(userDetails.getUserId(), roomId);
        Map<String, Object> response = new HashMap<>();
        response.put("isFavorite", isFavorite);
        response.put("message", isFavorite ? "Added to favorites" : "Removed from favorites");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add/{roomId}")
    public String addFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Long roomId) {
        favoriteService.addFavorite(userDetails.getUserId(), roomId);
        return "redirect:/rooms/" + roomId;
    }

    @PostMapping("/remove/{roomId}")
    public String removeFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long roomId) {
        favoriteService.removeFavorite(userDetails.getUserId(), roomId);
        return "redirect:/favorites";
    }
}
