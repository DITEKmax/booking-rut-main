package com.rut.booking.controllers;

import com.rut.booking.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        // If user is already logged in, redirect to home
        if (userDetails != null) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }

        return "pages/login";
    }
}
