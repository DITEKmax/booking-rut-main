package com.rut.booking.services;

import com.rut.booking.dto.DtoMapper;
import com.rut.booking.dto.ReviewCreateRequest;
import com.rut.booking.dto.ReviewDto;
import com.rut.booking.models.entities.Review;
import com.rut.booking.models.entities.Room;
import com.rut.booking.models.entities.User;
import com.rut.booking.models.exceptions.ResourceNotFoundException;
import com.rut.booking.repository.ReviewRepository;
import com.rut.booking.search.RoomSearchService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final DtoMapper dtoMapper;
    private final RoomSearchService roomSearchService;
    private final String uploadDir = "./uploads/reviews";

    public ReviewService(ReviewRepository reviewRepository, UserService userService,
                         RoomService roomService, DtoMapper dtoMapper,
                         @Lazy RoomSearchService roomSearchService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.roomService = roomService;
        this.dtoMapper = dtoMapper;
        this.roomSearchService = roomSearchService;
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
    }

    public ReviewDto getReviewById(Long id) {
        return dtoMapper.toReviewDto(findById(id));
    }

    public List<ReviewDto> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(dtoMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByRoom(Long roomId) {
        return reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(dtoMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAllOrderByCreatedAtDesc().stream()
                .map(dtoMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByRoomWithFilter(Long roomId, String sortBy, Integer rating, Boolean withPhotos) {
        List<Review> reviews;

        if (withPhotos != null && withPhotos) {
            reviews = reviewRepository.findByRoomIdWithPhotos(roomId);
        } else if (rating != null) {
            reviews = reviewRepository.findByRoomIdAndRating(roomId, rating);
        } else if (sortBy != null) {
            reviews = reviewRepository.findByRoomIdWithSort(roomId, sortBy);
        } else {
            reviews = reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        }

        return reviews.stream()
                .map(dtoMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    public Double getAverageRatingForRoom(Long roomId) {
        Double rating = reviewRepository.getAverageRatingForRoom(roomId);
        return rating != null ? Math.round(rating * 10.0) / 10.0 : 0.0;
    }

    public boolean hasUserReviewedRoom(Long userId, Long roomId) {
        return reviewRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    @Transactional
    public ReviewDto createReview(Long userId, ReviewCreateRequest request, MultipartFile image) {
        User user = userService.findById(userId);
        Room room = roomService.findById(request.getRoomId());

        // Check if user already reviewed this room
        if (hasUserReviewedRoom(userId, request.getRoomId())) {
            throw new IllegalStateException("You have already reviewed this room");
        }

        Review review = new Review();
        review.setUser(user);
        review.setRoom(room);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIssues(request.getIssues());

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            review.setImagePath(imagePath);
        }

        ReviewDto saved = dtoMapper.toReviewDto(reviewRepository.save(review));

        // Send notification to dispatcher if issues are reported
        if (request.getIssues() != null && !request.getIssues().trim().isEmpty()) {
            notifyDispatcherAboutIssues(review, room);
        }

        // Reindex room in Elasticsearch
        roomSearchService.reindexRoomAfterReview(request.getRoomId());

        return saved;
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, Long userId, ReviewCreateRequest request, MultipartFile image) {
        Review review = findById(reviewId);

        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIssues(request.getIssues());

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (review.getImagePath() != null) {
                deleteImage(review.getImagePath());
            }
            String imagePath = saveImage(image);
            review.setImagePath(imagePath);
        }

        ReviewDto saved = dtoMapper.toReviewDto(reviewRepository.save(review));

        // Send notification to dispatcher if issues are reported
        if (request.getIssues() != null && !request.getIssues().trim().isEmpty()) {
            notifyDispatcherAboutIssues(review, review.getRoom());
        }

        // Reindex room in Elasticsearch
        roomSearchService.reindexRoomAfterReview(review.getRoom().getId());

        return saved;
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = findById(reviewId);
        Long roomId = review.getRoom().getId();
        User currentUser = userService.findById(userId);

        // Check if user is the author or an admin
        boolean isAuthor = review.getUser().getId().equals(userId);
        boolean isAdmin = currentUser.getRole().getCode() == com.rut.booking.models.enums.RoleType.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new IllegalStateException("You can only delete your own reviews");
        }

        // Delete associated image
        if (review.getImagePath() != null) {
            deleteImage(review.getImagePath());
        }

        reviewRepository.delete(review);

        // Reindex room in Elasticsearch
        roomSearchService.reindexRoomAfterReview(roomId);
    }

    private String saveImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath);

            return "/uploads/reviews/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    private void deleteImage(String imagePath) {
        try {
            Path filePath = Paths.get("." + imagePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    private void notifyDispatcherAboutIssues(Review review, Room room) {
        // Log the issue for dispatcher notification
        String issuesList = review.getIssues();
        String userName = review.getUser().getFullName();
        String roomNumber = room.getNumber();

        System.out.println("=== DISPATCHER NOTIFICATION ===");
        System.out.println("Issues reported in room " + roomNumber + " by " + userName);
        System.out.println("Issues: " + issuesList);
        System.out.println("Review ID: " + review.getId());
        System.out.println("Please check the admin panel for details.");
        System.out.println("===============================");

        // TODO: In production, this should send an email or push notification to dispatcher
        // For now, we're just logging it to the console
    }
}
