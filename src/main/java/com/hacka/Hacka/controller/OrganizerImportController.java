package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.AnnouncementImportForm;
import com.hacka.Hacka.dto.HackathonForm;
import com.hacka.Hacka.dto.ParsedAnnouncement;
import com.hacka.Hacka.entity.HackathonMode;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.service.AnnouncementImportService;
import com.hacka.Hacka.service.OrganizationService;
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

/** Import d'une annonce par IA (UC-04) : texte collé -> formulaire de publication pré-rempli. */
@Controller
@RequestMapping("/organizer/import")
@RequiredArgsConstructor
public class OrganizerImportController {
    private final AnnouncementImportService importService;
    private final OrganizationService organizationService;
    private final ThemeService themeService;

    @GetMapping
    public String page(Model model) {
        model.addAttribute("importForm", new AnnouncementImportForm());
        return "organizer/import";
    }

    @PostMapping
    public String analyse(@Valid @ModelAttribute("importForm") AnnouncementImportForm importForm,
                          BindingResult result, Model model) {
        if (result.hasErrors()) return "organizer/import";
        try {
            ParsedAnnouncement p = importService.parse(importForm.getText());
            model.addAttribute("form", toForm(p));
            model.addAttribute("importedByAi", true);
            model.addAttribute("organizations", organizationService.findAll());
            model.addAttribute("themes", themeService.findAll());
            model.addAttribute("modes", HackathonMode.values());
            return "organizer/hackathon-form";
        } catch (BusinessException ex) {
            // Scénario alternatif UC-04 A1 : on reste sur la page (texte conservé) avec lien « saisie manuelle »
            model.addAttribute("error", ex.getMessage());
            return "organizer/import";
        }
    }

    private HackathonForm toForm(ParsedAnnouncement p) {
        HackathonForm f = new HackathonForm();
        f.setTitle(p.getTitle());
        f.setDescription(p.getDescription());
        f.setStartDate(p.getStartDate());
        f.setEndDate(p.getEndDate());
        f.setRegistrationDeadline(p.getRegistrationDeadline());
        f.setMaxParticipants(p.getMaxParticipants());
        f.setExternalLink(p.getExternalLink());
        if (p.getMode() != null) f.setMode(HackathonMode.valueOf(p.getMode()));
        if (f.getMode() != HackathonMode.ONLINE) {
            f.setLocationCity(p.getCity());
            f.setLocationAddress(p.getAddress());
        }
        f.setThemeIds(themeService.idsByNames(p.getThemes()));
        return f; // l'organisation reste à choisir : l'IA ne peut pas la deviner de façon fiable
    }
}
