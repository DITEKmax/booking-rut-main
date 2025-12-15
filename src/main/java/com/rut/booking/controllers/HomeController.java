package com.rut.booking.controllers;

import com.rut.booking.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/home";
    }

    @GetMapping("/contacts")
    public String contacts(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/contacts";
    }

    @GetMapping("/licenses")
    public String licenses(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/licenses";
    }

    @GetMapping("/policies")
    public String policies(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/policies";
    }

    @GetMapping("/faq")
    public String faq(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/faq";
    }

    @GetMapping("/error/403")
    public String accessDenied(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "error/403";
    }
}
