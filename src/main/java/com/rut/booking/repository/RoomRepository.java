package com.rut.booking.repository;

import com.rut.booking.models.entities.Room;
import com.rut.booking.models.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByNumber(String number);

    boolean existsByNumber(String number);

    List<Room> findByIsActiveTrue();

    List<Room> findByBuildingAndIsActiveTrue(String building);

    List<Room> findByFloorAndIsActiveTrue(Integer floor);

    List<Room> findByBuildingAndFloorAndIsActiveTrue(String building, Integer floor);

    List<Room> findByRoomTypeAndIsActiveTrue(RoomType roomType);

    @Query("SELECT r FROM Room r WHERE r.capacity >= :minCapacity AND r.isActive = true")
    List<Room> findByMinCapacity(@Param("minCapacity") Integer minCapacity);

    @Query("SELECT r FROM Room r WHERE " +
            "(LOWER(r.number) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND r.isActive = true")
    List<Room> searchRooms(@Param("search") String search);

    @Query("SELECT DISTINCT r.building FROM Room r WHERE r.isActive = true ORDER BY r.building")
    List<String> findAllBuildings();

    @Query("SELECT DISTINCT r.floor FROM Room r WHERE r.isActive = true ORDER BY r.floor")
    List<Integer> findAllFloors();

    @Query("SELECT DISTINCT r.floor FROM Room r WHERE r.building = :building AND r.isActive = true ORDER BY r.floor")
    List<Integer> findFloorsByBuilding(@Param("building") String building);

    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType AND r.capacity >= :capacity AND r.isActive = true AND r.id != :excludeId")
    List<Room> findSimilarRooms(@Param("roomType") RoomType roomType,
                                @Param("capacity") Integer capacity,
                                @Param("excludeId") Long excludeId);
}
