package com.rut.booking.repository;

import com.rut.booking.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND (r.isDeleted = false OR r.isDeleted IS NULL)")
    List<Review> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY r.createdAt DESC")
    List<Review> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND (r.isDeleted = false OR r.isDeleted IS NULL)")
    List<Review> findByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY r.createdAt DESC")
    List<Review> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId);

    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.room.id = :roomId AND (r.isDeleted = false OR r.isDeleted IS NULL)")
    Optional<Review> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.user.id = :userId AND r.room.id = :roomId AND (r.isDeleted = false OR r.isDeleted IS NULL)")
    boolean existsByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.room.id = :roomId AND (r.isDeleted = false OR r.isDeleted IS NULL)")
    Double getAverageRatingForRoom(@Param("roomId") Long roomId);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY " +
            "CASE WHEN :sortBy = 'rating' THEN r.rating END DESC, " +
            "CASE WHEN :sortBy = 'date' THEN r.createdAt END DESC")
    List<Review> findByRoomIdWithSort(@Param("roomId") Long roomId, @Param("sortBy") String sortBy);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND r.imagePath IS NOT NULL AND (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY r.createdAt DESC")
    List<Review> findByRoomIdWithPhotos(@Param("roomId") Long roomId);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND r.rating = :rating AND (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY r.createdAt DESC")
    List<Review> findByRoomIdAndRating(@Param("roomId") Long roomId, @Param("rating") Integer rating);

    @Query("SELECT r FROM Review r WHERE (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY r.createdAt DESC")
    List<Review> findAllOrderByCreatedAtDesc();

    @Query("SELECT r FROM Review r WHERE r.issues IS NOT NULL AND r.issues != '' AND (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY r.createdAt DESC")
    List<Review> findReviewsWithIssues();

    List<Review> findByIsDeletedTrue();
}
