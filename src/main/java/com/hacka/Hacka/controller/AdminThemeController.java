package com.hacka.Hacka.controller;

import com.hacka.Hacka.service.ThemeService;
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
@RequestMapping("/admin/themes")
@RequiredArgsConstructor
public class AdminThemeController {
    private final ThemeService themeService;   // le service de B

    @GetMapping
    public String list(Model model) {
        model.addAttribute("themes", themeService.findAll());
        return "admin/themes";
    }

    @PostMapping
    public String create(@RequestParam String name, RedirectAttributes ra) {
        themeService.create(name);
        ra.addFlashAttribute("success", "Thème ajouté.");
        return "redirect:/admin/themes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        themeService.delete(id);
        ra.addFlashAttribute("success", "Thème supprimé.");
        return "redirect:/admin/themes";
    }
}