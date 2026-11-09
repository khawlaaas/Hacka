package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.OrganizationForm;
import com.hacka.Hacka.entity.OrganizationType;
import com.hacka.Hacka.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Gestion des organisations (BF-13). */
@Controller
@RequestMapping("/organizer/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("form", new OrganizationForm());
        return page(model, null);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", OrganizationForm.from(organizationService.find(id)));
        return page(model, id);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") OrganizationForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return page(model, null);
        organizationService.create(form);
        ra.addFlashAttribute("success", "Organisation créée.");
        return "redirect:/organizer/organizations";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") OrganizationForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return page(model, id);
        organizationService.update(id, form);
        ra.addFlashAttribute("success", "Organisation modifiée.");
        return "redirect:/organizer/organizations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        organizationService.delete(id);
        ra.addFlashAttribute("success", "Organisation supprimée.");
        return "redirect:/organizer/organizations";
    }

    private String page(Model model, Long editId) {
        model.addAttribute("editId", editId);
        model.addAttribute("organizations", organizationService.findAll());
        model.addAttribute("types", OrganizationType.values());
        return "organizer/organizations";
    }
}
