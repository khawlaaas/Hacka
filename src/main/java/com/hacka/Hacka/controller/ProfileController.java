package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.ProfileDto;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.service.SkillService;
import com.hacka.Hacka.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final SkillService skillService;

    @GetMapping
    public String show(Principal principal, Model model) {
        User u = userService.getByEmail(principal.getName());
        model.addAttribute("user", u);
        model.addAttribute("profileDto", userService.toProfileDto(u));
        model.addAttribute("allSkills", skillService.findAll());
        return "auth/profile";
    }

    @PostMapping
    public String update(Principal principal,
                         @Valid @ModelAttribute("profileDto") ProfileDto dto,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.getByEmail(principal.getName()));
            model.addAttribute("allSkills", skillService.findAll());
            return "auth/profile";
        }
        userService.updateProfile(principal.getName(), dto);
        ra.addFlashAttribute("success", "Profil mis à jour.");
        return "redirect:/profile";
    }
}