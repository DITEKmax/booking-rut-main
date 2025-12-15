package com.rut.booking.repository;

import com.rut.booking.models.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.room WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithRooms(@Param("userId") Long userId);

    Optional<Favorite> findByUserIdAndRoomId(Long userId, Long roomId);

    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    void deleteByUserIdAndRoomId(Long userId, Long roomId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.room.id = :roomId")
    Long countByRoomId(@Param("roomId") Long roomId);
}
