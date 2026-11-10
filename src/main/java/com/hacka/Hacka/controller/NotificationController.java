package com.hacka.Hacka.controller;


import com.hacka.Hacka.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/profile/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public String list(Principal principal, Model model) {
        // 1) on lit la liste (états "non lu" conservés pour l'affichage)
        model.addAttribute("notifications", notificationService.listFor(principal.getName()));
        // 2) ensuite seulement on marque tout comme lu
        notificationService.markAllRead(principal.getName());
        return "auth/notifications";
    }
}
