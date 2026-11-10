package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.RegisterDto;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto dto,
                           BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "auth/register";
        try {
            userService.register(dto);
        } catch (BusinessException e) {
            result.reject("business", e.getMessage());
            return "auth/register";
        }
        ra.addFlashAttribute("success", "Compte créé, vous pouvez vous connecter.");
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String denied() { return "error/403"; }
}