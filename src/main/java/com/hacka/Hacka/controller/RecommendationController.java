package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.CurrentUserResolver;
import com.hacka.Hacka.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final CurrentUserResolver currentUser;

    @GetMapping("/my/recommendations")
    public String recommendations(Principal principal, Model model) {
        model.addAttribute("items", recommendationService.recommend(currentUser.get(principal).getId(), 10));
        return "participation/recommendations";
    }
}