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

    List<Review> findByUserId(Long userId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByRoomId(Long roomId);

    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    Optional<Review> findByUserIdAndRoomId(Long userId, Long roomId);

    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.room.id = :roomId")
    Double getAverageRatingForRoom(@Param("roomId") Long roomId);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId ORDER BY " +
            "CASE WHEN :sortBy = 'rating' THEN r.rating END DESC, " +
            "CASE WHEN :sortBy = 'date' THEN r.createdAt END DESC")
    List<Review> findByRoomIdWithSort(@Param("roomId") Long roomId, @Param("sortBy") String sortBy);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND r.imagePath IS NOT NULL ORDER BY r.createdAt DESC")
    List<Review> findByRoomIdWithPhotos(@Param("roomId") Long roomId);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND r.rating = :rating ORDER BY r.createdAt DESC")
    List<Review> findByRoomIdAndRating(@Param("roomId") Long roomId, @Param("rating") Integer rating);
}
