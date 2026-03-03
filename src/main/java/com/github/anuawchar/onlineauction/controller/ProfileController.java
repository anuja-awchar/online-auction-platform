package com.github.anuawchar.onlineauction.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.service.UserProfileService;

@Controller
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        User user = userProfileService.getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }
}
