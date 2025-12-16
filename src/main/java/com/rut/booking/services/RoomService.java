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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final FavoriteRepository favoriteRepository;
    private final DtoMapper dtoMapper;
    private final RoomSearchService roomSearchService;

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
}
