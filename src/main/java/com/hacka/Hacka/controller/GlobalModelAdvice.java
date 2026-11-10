package com.hacka.Hacka.controller;


import com.hacka.Hacka.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {
    private final NotificationService notificationService;

    @ModelAttribute("unreadCount")
    public long unreadCount(Principal principal) {
        return principal == null ? 0 : notificationService.unreadCount(principal.getName());
    }
}
