package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.CurrentUserResolver;
import com.hacka.Hacka.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final CurrentUserResolver currentUser;

    @PostMapping("/hackathons/{id}/register")
    public String register(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        registrationService.register(currentUser.get(principal).getId(), id);
        ra.addFlashAttribute("success", "Inscription confirmée !");
        return "redirect:/hackathons/" + id;
    }

    @PostMapping("/hackathons/{id}/unregister")
    public String unregister(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        registrationService.cancel(currentUser.get(principal).getId(), id);
        ra.addFlashAttribute("success", "Inscription annulée.");
        return "redirect:/hackathons/" + id;
    }

    @GetMapping("/my/registrations")
    public String mine(Principal principal, Model model) {
        model.addAttribute("registrations", registrationService.history(currentUser.get(principal).getId()));
        return "participation/my-registrations";
    }
}