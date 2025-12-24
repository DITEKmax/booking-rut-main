package com.rut.booking.services;

import com.rut.booking.dto.DtoMapper;
import com.rut.booking.dto.RoomDto;
import com.rut.booking.models.entities.Room;
import com.rut.booking.models.enums.ClassPeriod;
import com.rut.booking.models.enums.RoomType;
import com.rut.booking.models.exceptions.ResourceNotFoundException;
import com.rut.booking.repository.BookingRepository;
import com.rut.booking.repository.FavoriteRepository;
import com.rut.booking.repository.RoomRepository;
import com.rut.booking.search.RoomSearchService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final FavoriteRepository favoriteRepository;
    private final DtoMapper dtoMapper;
    private final RoomSearchService roomSearchService;
    private final String uploadDir = "./uploads/rooms";

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository,
                       FavoriteRepository favoriteRepository, DtoMapper dtoMapper,
                       @Lazy RoomSearchService roomSearchService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.favoriteRepository = favoriteRepository;
        this.dtoMapper = dtoMapper;
        this.roomSearchService = roomSearchService;
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    public RoomDto getRoomById(Long id) {
        return dtoMapper.toRoomDto(findById(id));
    }

    public RoomDto getRoomById(Long id, Long userId) {
        Room room = findById(id);
        boolean isFavorite = userId != null && favoriteRepository.existsByUserIdAndRoomId(userId, id);
        return dtoMapper.toRoomDto(room, isFavorite);
    }

    public List<RoomDto> getAllActiveRooms() {
        return roomRepository.findByIsActiveTrue().stream()
                .map(dtoMapper::toRoomDto)
                .collect(Collectors.toList());
    }

    public List<RoomDto> getAllActiveRooms(Long userId) {
        return roomRepository.findByIsActiveTrue().stream()
                .map(room -> {
                    boolean isFavorite = userId != null &&
                            favoriteRepository.existsByUserIdAndRoomId(userId, room.getId());
                    return dtoMapper.toRoomDto(room, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public List<RoomDto> searchRooms(String search) {
        // Try Elasticsearch first
        List<Long> elasticResults = roomSearchService.searchRooms(search);
        if (!elasticResults.isEmpty()) {
            return elasticResults.stream()
                    .map(id -> roomRepository.findById(id).orElse(null))
                    .filter(room -> room != null)
                    .map(dtoMapper::toRoomDto)
                    .collect(Collectors.toList());
        }

        // Fallback to database search
        return roomRepository.searchRooms(search).stream()
                .map(dtoMapper::toRoomDto)
                .collect(Collectors.toList());
    }

    public List<RoomDto> filterRooms(String building, Integer floor, Long userId) {
        List<Room> rooms;
        if (building != null && !building.isEmpty() && floor != null) {
            rooms = roomRepository.findByBuildingAndFloorAndIsActiveTrue(building, floor);
        } else if (building != null && !building.isEmpty()) {
            rooms = roomRepository.findByBuildingAndIsActiveTrue(building);
        } else if (floor != null) {
            rooms = roomRepository.findByFloorAndIsActiveTrue(floor);
        } else {
            rooms = roomRepository.findByIsActiveTrue();
        }

        return rooms.stream()
                .map(room -> {
                    boolean isFavorite = userId != null &&
                            favoriteRepository.existsByUserIdAndRoomId(userId, room.getId());
                    return dtoMapper.toRoomDto(room, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public List<RoomDto> filterRoomsWithAvailability(String building, Integer floor,
                                                      LocalDate date, ClassPeriod period, Long userId) {
        // Start with basic building/floor filter
        List<Room> rooms;
        if (building != null && !building.isEmpty() && floor != null) {
            rooms = roomRepository.findByBuildingAndFloorAndIsActiveTrue(building, floor);
        } else if (building != null && !building.isEmpty()) {
            rooms = roomRepository.findByBuildingAndIsActiveTrue(building);
        } else if (floor != null) {
            rooms = roomRepository.findByFloorAndIsActiveTrue(floor);
        } else {
            rooms = roomRepository.findByIsActiveTrue();
        }

        // Apply availability filter if date or period is specified
        List<RoomDto> result = new ArrayList<>();
        for (Room room : rooms) {
            boolean isAvailable = true;

            if (date != null && period != null) {
                // Check if room is available for specific date and period
                isAvailable = !bookingRepository.isRoomBookedForPeriod(room.getId(), date, period);
            } else if (date != null) {
                // Check if room has any available period on this date
                boolean hasAvailablePeriod = false;
                for (ClassPeriod p : ClassPeriod.values()) {
                    if (!bookingRepository.isRoomBookedForPeriod(room.getId(), date, p)) {
                        hasAvailablePeriod = true;
                        break;
                    }
                }
                isAvailable = hasAvailablePeriod;
            } else if (period != null) {
                // Check if room is available for this period today or in the future
                LocalDate today = LocalDate.now();
                isAvailable = !bookingRepository.isRoomBookedForPeriod(room.getId(), today, period);
            }

            if (isAvailable) {
                boolean isFavorite = userId != null &&
                        favoriteRepository.existsByUserIdAndRoomId(userId, room.getId());
                result.add(dtoMapper.toRoomDto(room, isFavorite));
            }
        }

        return result;
    }

    public List<RoomDto> getAvailableRooms(LocalDate date, ClassPeriod period, Long userId) {
        List<Room> allRooms = roomRepository.findByIsActiveTrue();
        List<RoomDto> availableRooms = new ArrayList<>();

        for (Room room : allRooms) {
            boolean isBooked = bookingRepository.isRoomBookedForPeriod(room.getId(), date, period);
            if (!isBooked) {
                boolean isFavorite = userId != null &&
                        favoriteRepository.existsByUserIdAndRoomId(userId, room.getId());
                availableRooms.add(dtoMapper.toRoomDto(room, isFavorite));
            }
        }

        return availableRooms;
    }

    public List<ClassPeriod> getAvailablePeriods(Long roomId, LocalDate date) {
        List<ClassPeriod> availablePeriods = new ArrayList<>();
        for (ClassPeriod period : ClassPeriod.values()) {
            boolean isBooked = bookingRepository.isRoomBookedForPeriod(roomId, date, period);
            if (!isBooked) {
                availablePeriods.add(period);
            }
        }
        return availablePeriods;
    }

    public List<RoomDto> getSimilarRooms(Long roomId, int limit) {
        Room room = findById(roomId);
        return roomRepository.findSimilarRooms(room.getRoomType(), room.getCapacity(), roomId)
                .stream()
                .limit(limit)
                .map(dtoMapper::toRoomDto)
                .collect(Collectors.toList());
    }

    public List<RoomDto> getSimilarRoomsWithAvailability(Long roomId, LocalDate date, ClassPeriod period,
                                                          int offset, int limit, Long userId) {
        Room targetRoom = findById(roomId);
        List<Room> allRooms = roomRepository.findByIsActiveTrue();

        // Calculate suitability score for each room
        List<RoomWithScore> scoredRooms = new ArrayList<>();
        for (Room room : allRooms) {
            if (room.getId().equals(roomId)) continue; // Skip the target room itself

            int score = calculateSuitabilityScore(targetRoom, room, date, period);
            scoredRooms.add(new RoomWithScore(room, score));
        }

        // Sort by score (descending) and convert to DTOs
        return scoredRooms.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .skip(offset)
                .limit(limit)
                .map(rws -> {
                    boolean isFavorite = userId != null &&
                            favoriteRepository.existsByUserIdAndRoomId(userId, rws.room.getId());
                    RoomDto dto = dtoMapper.toRoomDto(rws.room, isFavorite);
                    // Add availability info
                    if (date != null && period != null) {
                        boolean isAvailable = !bookingRepository.isRoomBookedForPeriod(rws.room.getId(), date, period);
                        dto.setIsAvailable(isAvailable);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private int calculateSuitabilityScore(Room targetRoom, Room candidateRoom, LocalDate date, ClassPeriod period) {
        int score = 0;

        // Same room type: +100 points
        if (candidateRoom.getRoomType() == targetRoom.getRoomType()) {
            score += 100;
        }

        // Capacity similarity (within 20%): +50 points, (within 50%): +25 points
        int targetCapacity = targetRoom.getCapacity();
        int candidateCapacity = candidateRoom.getCapacity();
        double capacityDiff = Math.abs((double)(candidateCapacity - targetCapacity) / targetCapacity);
        if (capacityDiff <= 0.2) {
            score += 50;
        } else if (capacityDiff <= 0.5) {
            score += 25;
        }

        // Equipment matching
        if (targetRoom.getHasProjector() && candidateRoom.getHasProjector()) score += 20;
        if (targetRoom.getHasComputers() && candidateRoom.getHasComputers()) score += 20;
        if (targetRoom.getHasWhiteboard() && candidateRoom.getHasWhiteboard()) score += 10;

        // Same building: +30 points
        if (candidateRoom.getBuilding().equals(targetRoom.getBuilding())) {
            score += 30;
        }

        // Same floor: +15 points
        if (candidateRoom.getFloor().equals(targetRoom.getFloor())) {
            score += 15;
        }

        // Available for requested date/period: +200 points (highest priority if date/period specified)
        if (date != null && period != null) {
            boolean isAvailable = !bookingRepository.isRoomBookedForPeriod(candidateRoom.getId(), date, period);
            if (isAvailable) {
                score += 200;
            } else {
                score -= 50; // Penalize unavailable rooms
            }
        }

        // Higher rating: up to +30 points
        Double avgRating = candidateRoom.getAverageRating();
        if (avgRating != null && avgRating > 0) {
            score += (int)(avgRating * 6); // 5.0 rating = 30 points
        }

        return score;
    }

    // Helper class for scoring
    private static class RoomWithScore {
        Room room;
        int score;

        RoomWithScore(Room room, int score) {
            this.room = room;
            this.score = score;
        }
    }

    public List<String> getAllBuildings() {
        return roomRepository.findAllBuildings();
    }

    public List<Integer> getAllFloors() {
        return roomRepository.findAllFloors();
    }

    public List<Integer> getFloorsByBuilding(String building) {
        return roomRepository.findFloorsByBuilding(building);
    }

    public List<RoomDto> getRoomsByType(RoomType roomType) {
        return roomRepository.findByRoomTypeAndIsActiveTrue(roomType).stream()
                .map(dtoMapper::toRoomDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    @Transactional
    public RoomDto createRoom(String number, RoomType roomType, Integer capacity,
                             Boolean hasComputers, Boolean hasProjector, Boolean hasWhiteboard,
                             String description, MultipartFile image) {
        Room room = new Room();
        room.setNumber(number);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setHasComputers(hasComputers != null ? hasComputers : false);
        room.setHasProjector(hasProjector != null ? hasProjector : false);
        room.setHasWhiteboard(hasWhiteboard != null ? hasWhiteboard : false);
        room.setDescription(description);
        room.setIsActive(true);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imagePath = saveRoomImage(image);
            room.setImagePath(imagePath);
        }

        Room saved = roomRepository.save(room);

        // Index in Elasticsearch
        roomSearchService.indexRoom(saved.getId());

        return dtoMapper.toRoomDto(saved);
    }

    @Transactional
    public RoomDto updateRoom(Long roomId, String number, RoomType roomType, Integer capacity,
                             Boolean hasComputers, Boolean hasProjector, Boolean hasWhiteboard,
                             String description, Boolean isActive, MultipartFile image) {
        Room room = findById(roomId);

        room.setNumber(number);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setHasComputers(hasComputers != null ? hasComputers : false);
        room.setHasProjector(hasProjector != null ? hasProjector : false);
        room.setHasWhiteboard(hasWhiteboard != null ? hasWhiteboard : false);
        room.setDescription(description);
        room.setIsActive(isActive != null ? isActive : true);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (room.getImagePath() != null) {
                deleteRoomImage(room.getImagePath());
            }
            String imagePath = saveRoomImage(image);
            room.setImagePath(imagePath);
        }

        Room saved = roomRepository.save(room);

        // Reindex in Elasticsearch
        roomSearchService.indexRoom(saved.getId());

        return dtoMapper.toRoomDto(saved);
    }

    private String saveRoomImage(MultipartFile file) {
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

            return "/uploads/rooms/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save room image: " + e.getMessage(), e);
        }
    }

    private void deleteRoomImage(String imagePath) {
        try {
            Path filePath = Paths.get("." + imagePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete room image: " + e.getMessage());
        }
    }
}
