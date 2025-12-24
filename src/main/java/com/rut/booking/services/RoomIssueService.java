package com.rut.booking.services;

import com.rut.booking.dto.DtoMapper;
import com.rut.booking.dto.RoomIssueCreateRequest;
import com.rut.booking.dto.RoomIssueDto;
import com.rut.booking.models.entities.RoomIssue;
import com.rut.booking.models.entities.Room;
import com.rut.booking.models.entities.User;
import com.rut.booking.models.exceptions.ResourceNotFoundException;
import com.rut.booking.repository.RoomIssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RoomIssueService {

    private final RoomIssueRepository roomIssueRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final DtoMapper dtoMapper;
    private final String uploadDir = "./uploads/issues";

    public RoomIssueService(RoomIssueRepository roomIssueRepository, UserService userService,
                            RoomService roomService, DtoMapper dtoMapper) {
        this.roomIssueRepository = roomIssueRepository;
        this.userService = userService;
        this.roomService = roomService;
        this.dtoMapper = dtoMapper;
    }

    public RoomIssue findById(Long id) {
        return roomIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomIssue", id));
    }

    public RoomIssueDto getIssueById(Long id) {
        return dtoMapper.toRoomIssueDto(findById(id));
    }

    public List<RoomIssueDto> getIssuesByUser(Long userId) {
        return roomIssueRepository.findByUserId(userId).stream()
                .map(dtoMapper::toRoomIssueDto)
                .collect(Collectors.toList());
    }

    public List<RoomIssueDto> getAllIssues() {
        return roomIssueRepository.findAllIssues().stream()
                .map(dtoMapper::toRoomIssueDto)
                .collect(Collectors.toList());
    }

    public List<RoomIssueDto> getUnresolvedIssues() {
        return roomIssueRepository.findUnresolvedIssues().stream()
                .map(dtoMapper::toRoomIssueDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomIssueDto createIssue(Long userId, RoomIssueCreateRequest request, MultipartFile image) {
        User user = userService.findById(userId);
        Room room = roomService.findById(request.getRoomId());

        RoomIssue issue = new RoomIssue();
        issue.setUser(user);
        issue.setRoom(room);
        issue.setIssues(request.getIssues());
        issue.setDescription(request.getDescription());

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            issue.setImagePath(imagePath);
        }

        RoomIssueDto saved = dtoMapper.toRoomIssueDto(roomIssueRepository.save(issue));

        // Notify dispatcher
        notifyDispatcherAboutIssues(issue, room);

        return saved;
    }

    @Transactional
    public void markIssueAsResolved(Long issueId, Long userId) {
        RoomIssue issue = findById(issueId);
        User resolvedBy = userService.findById(userId);

        issue.setIsResolved(true);
        issue.setResolvedAt(LocalDateTime.now());
        issue.setResolvedBy(resolvedBy);
        roomIssueRepository.save(issue);
    }

    @Transactional
    public void deleteIssue(Long issueId, Long userId) {
        RoomIssue issue = findById(issueId);
        User currentUser = userService.findById(userId);

        // Check if user is the author or an admin
        boolean isAuthor = issue.getUser().getId().equals(userId);
        boolean isAdmin = currentUser.getRole().getCode() == com.rut.booking.models.enums.RoleType.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new IllegalStateException("You can only delete your own issue reports");
        }

        roomIssueRepository.delete(issue);
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

            return "/uploads/issues/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    private void notifyDispatcherAboutIssues(RoomIssue issue, Room room) {
        // Log the issue for dispatcher notification
        String issuesList = issue.getIssues();
        String userName = issue.getUser().getFullName();
        String roomNumber = room.getNumber();

        System.out.println("=== DISPATCHER NOTIFICATION ===");
        System.out.println("Issues reported in room " + roomNumber + " by " + userName);
        System.out.println("Issues: " + issuesList);
        System.out.println("Issue ID: " + issue.getId());
        System.out.println("Please check the admin panel for details.");
        System.out.println("===============================");
    }
}
