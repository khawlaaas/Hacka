package com.hacka.Hacka.controller;


import com.hacka.Hacka.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/skills")
@RequiredArgsConstructor
public class AdminSkillController {
    private final SkillService skillService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("skills", skillService.findAll());
        return "admin/skills";
    }

    @PostMapping
    public String create(@RequestParam String name, RedirectAttributes ra) {
        skillService.create(name);
        ra.addFlashAttribute("success", "Compétence ajoutée.");
        return "redirect:/admin/skills";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        skillService.delete(id);
        ra.addFlashAttribute("success", "Compétence supprimée.");
        return "redirect:/admin/skills";
    }
}
