package com.hacka.Hacka.controller;

import ch.qos.logback.core.model.Model;
import com.hacka.Hacka.entity.Role;
import com.hacka.Hacka.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;


@Controller
@RequestMapping("/admin/users") @RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService service;

    @GetMapping
    public String list(Model m) { m.addAttribute("users", service.all()); m.addAttribute("roles", Role.values()); return "admin/users"; }

    @PostMapping("/{id}/role")
    public String role(@PathVariable Long id, @RequestParam Role role, Principal p, RedirectAttributes ra) {
        service.changeRole(id, role, p.getName());
        ra.addFlashAttribute("success", "Rôle modifié.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Principal p, RedirectAttributes ra) {
        service.toggleEnabled(id, p.getName());
        ra.addFlashAttribute("success", "Statut du compte modifié.");
        return "redirect:/admin/users";
    }
}
