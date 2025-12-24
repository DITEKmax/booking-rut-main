package com.rut.booking.controllers;

import com.rut.booking.dto.RoomDto;
import com.rut.booking.dto.RoomIssueCreateRequest;
import com.rut.booking.dto.RoomIssueDto;
import com.rut.booking.security.CustomUserDetails;
import com.rut.booking.services.RoomIssueService;
import com.rut.booking.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/room-issues")
public class RoomIssueController {

    private final RoomIssueService roomIssueService;
    private final RoomService roomService;

    public RoomIssueController(RoomIssueService roomIssueService, RoomService roomService) {
        this.roomIssueService = roomIssueService;
        this.roomService = roomService;
    }

    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestParam(required = false) Long roomId,
                                  Model model) {
        RoomIssueCreateRequest request = new RoomIssueCreateRequest();
        if (roomId != null) {
            request.setRoomId(roomId);
            RoomDto room = roomService.getRoomById(roomId);
            model.addAttribute("room", room);
        }

        List<RoomDto> rooms = roomService.getAllActiveRooms();

        model.addAttribute("issueRequest", request);
        model.addAttribute("rooms", rooms);
        model.addAttribute("user", userDetails);

        return "pages/room-issues/form";
    }

    @PostMapping("/create")
    public String createIssue(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @Valid @ModelAttribute("issueRequest") RoomIssueCreateRequest request,
                             BindingResult bindingResult,
                             @RequestParam(required = false) MultipartFile image,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            List<RoomDto> rooms = roomService.getAllActiveRooms();
            model.addAttribute("rooms", rooms);
            model.addAttribute("user", userDetails);
            return "pages/room-issues/form";
        }

        try {
            roomIssueService.createIssue(userDetails.getUserId(), request, image);
            redirectAttributes.addFlashAttribute("success", "Проблема успешно отправлена диспетчеру");
            return "redirect:/rooms/" + request.getRoomId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось отправить сообщение о проблеме: " + e.getMessage());
            return "redirect:/room-issues/create?roomId=" + request.getRoomId();
        }
    }

    @GetMapping("/my-issues")
    public String myIssues(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<RoomIssueDto> issues = roomIssueService.getIssuesByUser(userDetails.getUserId());

        model.addAttribute("issues", issues);
        model.addAttribute("user", userDetails);

        return "pages/room-issues/my-issues";
    }

    @PostMapping("/{id}/delete")
    public String deleteIssue(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            roomIssueService.deleteIssue(id, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("success", "Сообщение о проблеме удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить сообщение: " + e.getMessage());
        }
        return "redirect:/room-issues/my-issues";
    }
}
