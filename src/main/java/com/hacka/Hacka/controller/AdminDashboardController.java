package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// controller/AdminDashboardController.java
@Controller
@RequiredArgsConstructor
public class AdminDashboardController {
    private final DashboardService service;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model m) {
        m.addAttribute("usersByRole", service.usersByRole());
        m.addAttribute("byStatus", service.hackathonsByStatus());
        m.addAttribute("byTheme", service.hackathonsByTheme());
        m.addAttribute("byRegistration", service.registrationsByStatus());
        return "admin/dashboard";
    }
}
