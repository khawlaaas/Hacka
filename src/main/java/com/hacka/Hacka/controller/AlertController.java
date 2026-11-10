package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.AlertDto;
import com.hacka.Hacka.entity.HackathonMode;
import com.hacka.Hacka.service.AlertService;
import com.hacka.Hacka.service.ThemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;
    private final ThemeService themeService;

    @GetMapping
    public String list(Principal p, Model m) {
        m.addAttribute("alerts", alertService.listFor(p.getName()));
        m.addAttribute("alertDto", new AlertDto());
        m.addAttribute("themes", themeService.findAll());
        m.addAttribute("modes", HackathonMode.values());
        return "auth/alerts";
    }

    @PostMapping
    public String create(Principal p, @Valid @ModelAttribute("alertDto") AlertDto dto,
                         BindingResult result, Model m, RedirectAttributes ra) {
        if (result.hasErrors()) {
            m.addAttribute("alerts", alertService.listFor(p.getName()));
            m.addAttribute("themes", themeService.findAll());
            m.addAttribute("modes", HackathonMode.values());
            return "auth/alerts";
        }
        alertService.create(p.getName(), dto);
        ra.addFlashAttribute("success", "Alerte créée.");
        return "redirect:/profile/alerts";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Principal p, RedirectAttributes ra) {
        alertService.toggle(p.getName(), id);
        ra.addFlashAttribute("success", "Alerte mise à jour.");
        return "redirect:/profile/alerts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal p, RedirectAttributes ra) {
        alertService.delete(p.getName(), id);
        ra.addFlashAttribute("success", "Alerte supprimée.");
        return "redirect:/profile/alerts";
    }
}
