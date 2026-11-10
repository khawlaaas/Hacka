package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.AdminHackathonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hackathons")
@RequiredArgsConstructor
public class AdminHackathonController {
    private final AdminHackathonService adminHackathonService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hackathons", adminHackathonService.pending());
        return "admin/hackathons";
    }

    @PostMapping("/{id}/validate")
    public String validate(@PathVariable Long id, RedirectAttributes ra) {
        adminHackathonService.validate(id);
        ra.addFlashAttribute("success", "Hackathon validé.");
        return "redirect:/admin/hackathons";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String reason,
                         RedirectAttributes ra) {
        adminHackathonService.reject(id, reason);
        ra.addFlashAttribute("success", "Hackathon refusé.");
        return "redirect:/admin/hackathons";
    }
}