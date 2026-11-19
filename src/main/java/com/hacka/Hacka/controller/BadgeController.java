package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.BadgeService;
import com.hacka.Hacka.service.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final CurrentUserResolver currentUser;

    @GetMapping("/my/badges")
    public String badges(Principal principal, Model model) {
        model.addAttribute("badges", badgeService.badgesOf(currentUser.get(principal).getId()));
        return "participation/badges";
    }
}