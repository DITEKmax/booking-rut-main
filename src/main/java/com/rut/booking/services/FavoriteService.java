package com.rut.booking.services;

import com.rut.booking.dto.DtoMapper;
import com.rut.booking.dto.RoomDto;
import com.rut.booking.models.entities.Favorite;
import com.rut.booking.models.entities.Room;
import com.rut.booking.models.entities.User;
import com.rut.booking.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final DtoMapper dtoMapper;

    public FavoriteService(FavoriteRepository favoriteRepository, UserService userService,
                           RoomService roomService, DtoMapper dtoMapper) {
        this.favoriteRepository = favoriteRepository;
        this.userService = userService;
        this.roomService = roomService;
        this.dtoMapper = dtoMapper;
    }

    public List<RoomDto> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserIdWithRooms(userId).stream()
                .map(favorite -> {
                    RoomDto dto = dtoMapper.toRoomDto(favorite.getRoom());
                    dto.setIsFavorite(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public boolean isFavorite(Long userId, Long roomId) {
        return favoriteRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long roomId) {
        if (favoriteRepository.existsByUserIdAndRoomId(userId, roomId)) {
            favoriteRepository.deleteByUserIdAndRoomId(userId, roomId);
            return false;
        } else {
            User user = userService.findById(userId);
            Room room = roomService.findById(roomId);
            Favorite favorite = new Favorite(user, room);
            favoriteRepository.save(favorite);
            return true;
        }
    }

    @Transactional
    public void addFavorite(Long userId, Long roomId) {
        if (!favoriteRepository.existsByUserIdAndRoomId(userId, roomId)) {
            User user = userService.findById(userId);
            Room room = roomService.findById(roomId);
            Favorite favorite = new Favorite(user, room);
            favoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void removeFavorite(Long userId, Long roomId) {
        favoriteRepository.deleteByUserIdAndRoomId(userId, roomId);
    }

    public Long countFavoritesForRoom(Long roomId) {
        return favoriteRepository.countByRoomId(roomId);
    }
}
