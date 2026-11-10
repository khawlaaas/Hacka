package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/hackathons/{id}/report")
    public String report(@PathVariable Long id, @RequestParam String reason,
                         Principal principal, RedirectAttributes ra) {
        reportService.create(principal.getName(), id, reason);
        ra.addFlashAttribute("success", "Merci, votre signalement a été transmis à l'administrateur.");
        return "redirect:/hackathons/" + id;
    }
}
