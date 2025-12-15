package com.rut.booking.services;

import com.rut.booking.dto.DtoMapper;
import com.rut.booking.dto.UserDto;
import com.rut.booking.models.entities.User;
import com.rut.booking.models.enums.RoleType;
import com.rut.booking.models.exceptions.ResourceNotFoundException;
import com.rut.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    public UserService(UserRepository userRepository, DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public UserDto getUserById(Long id) {
        return dtoMapper.toUserDto(findById(id));
    }

    public UserDto getUserByEmail(String email) {
        return dtoMapper.toUserDto(findByEmail(email));
    }

    public List<UserDto> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(dtoMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersByRole(RoleType roleType) {
        return userRepository.findByRoleType(roleType).stream()
                .map(dtoMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> searchUsers(String search) {
        return userRepository.searchUsers(search).stream()
                .map(dtoMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
