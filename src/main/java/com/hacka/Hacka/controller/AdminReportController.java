package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final ReportService reportService;

    @GetMapping
    public String list(Model m) { m.addAttribute("reports", reportService.all()); return "admin/reports"; }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id, RedirectAttributes ra) {
        reportService.resolve(id);
        ra.addFlashAttribute("success", "Signalement clôturé.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/{id}/cancel-hackathon")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        reportService.resolveAndCancel(id);
        ra.addFlashAttribute("success", "Signalement clôturé et hackathon retiré (annulé).");
        return "redirect:/admin/reports";
    }
}
