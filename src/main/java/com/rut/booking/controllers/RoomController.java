package com.rut.booking.controllers;

import com.rut.booking.dto.ReviewDto;
import com.rut.booking.dto.RoomDto;
import com.rut.booking.models.enums.ClassPeriod;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.FavoriteService;
import com.rut.booking.services.ReviewService;
import com.rut.booking.services.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    public RoomController(RoomService roomService, ReviewService reviewService,
                          FavoriteService favoriteService) {
        this.roomService = roomService;
        this.reviewService = reviewService;
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public String listRooms(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String building,
                            @RequestParam(required = false) Integer floor,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            @RequestParam(required = false) ClassPeriod period,
                            Model model) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        List<RoomDto> rooms;
        if (search != null && !search.isEmpty()) {
            rooms = roomService.searchRooms(search);
        } else if (date != null || period != null || building != null || floor != null) {
            rooms = roomService.filterRoomsWithAvailability(building, floor, date, period, userId);
        } else {
            rooms = roomService.getAllActiveRooms(userId);
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("buildings", roomService.getAllBuildings());
        model.addAttribute("floors", roomService.getAllFloors());
        model.addAttribute("periods", ClassPeriod.values());
        model.addAttribute("search", search);
        model.addAttribute("selectedBuilding", building);
        model.addAttribute("selectedFloor", floor);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("user", userDetails);

        return "pages/rooms/list";
    }

    @GetMapping("/{id}")
    public String roomDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Long id,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) Integer rating,
                              @RequestParam(required = false) Boolean withPhotos,
                              Model model) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        RoomDto room = roomService.getRoomById(id, userId);
        List<ReviewDto> reviews = reviewService.getReviewsByRoomWithFilter(id, sortBy, rating, withPhotos);
        List<RoomDto> similarRooms = roomService.getSimilarRooms(id, 5);
        Double averageRating = reviewService.getAverageRatingForRoom(id);
        boolean hasReviewed = userId != null && reviewService.hasUserReviewedRoom(userId, id);

        model.addAttribute("room", room);
        model.addAttribute("reviews", reviews);
        model.addAttribute("similarRooms", similarRooms);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("hasReviewed", hasReviewed);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("ratingFilter", rating);
        model.addAttribute("withPhotos", withPhotos);
        model.addAttribute("user", userDetails);

        return "pages/rooms/details";
    }

    @GetMapping("/available")
    public String availableRooms(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam ClassPeriod period,
                                 Model model) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        List<RoomDto> rooms = roomService.getAvailableRooms(date, period, userId);

        model.addAttribute("rooms", rooms);
        model.addAttribute("date", date);
        model.addAttribute("period", period);
        model.addAttribute("periods", ClassPeriod.values());
        model.addAttribute("user", userDetails);

        return "pages/rooms/available";
    }

    @GetMapping("/api/floors")
    @ResponseBody
    public ResponseEntity<List<Integer>> getFloorsByBuilding(@RequestParam String building) {
        return ResponseEntity.ok(roomService.getFloorsByBuilding(building));
    }

    @GetMapping("/api/available-periods/{roomId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAvailablePeriods(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ClassPeriod> periods = roomService.getAvailablePeriods(roomId, date);
        List<Map<String, Object>> result = periods.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", p.name());
            map.put("displayName", p.getDisplayName());
            map.put("timeRange", p.getTimeRange());
            map.put("number", p.getNumber());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/similar-rooms/{roomId}")
    @ResponseBody
    public ResponseEntity<List<RoomDto>> getSimilarRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ClassPeriod period,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        List<RoomDto> similarRooms = roomService.getSimilarRoomsWithAvailability(
                roomId, date, period, offset, limit, userId);
        return ResponseEntity.ok(similarRooms);
    }
}
