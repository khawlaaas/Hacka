package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.HackathonForm;
import com.hacka.Hacka.dto.PrizeForm;
import com.hacka.Hacka.dto.SponsorForm;
import com.hacka.Hacka.entity.*;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/** Espace organisateur : publier, modifier, annuler, inscrits, prix et sponsors. */
@Controller
@RequestMapping("/organizer/hackathons")
@RequiredArgsConstructor
public class OrganizerHackathonController {
    private final HackathonService hackathonService;
    private final OrganizationService organizationService;
    private final ThemeService themeService;
    private final PrizeService prizeService;
    private final SponsorService sponsorService;
    private final RegistrantService registrantService;
    private final CurrentUserResolver currentUser;

    // ---------- liste ----------
    @GetMapping
    public String mine(Principal principal, Model model) {
        model.addAttribute("hackathons", hackathonService.mine(currentUser.get(principal).getId()));
        return "organizer/hackathons";
    }

    // ---------- création ----------
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new HackathonForm());
        return formPage(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") HackathonForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes ra) {
        if (!result.hasErrors() && !form.isIgnoreDuplicates()) {
            List<Hackathon> duplicates = hackathonService.findPossibleDuplicates(form.getTitle(), form.getStartDate());
            if (!duplicates.isEmpty()) {
                model.addAttribute("duplicates", duplicates);
                result.reject("duplicate", "Un hackathon similaire existe peut-être déjà (voir ci-dessous). "
                        + "Cochez la case pour publier quand même.");
            }
        }
        if (!result.hasErrors()) {
            try {
                hackathonService.create(form, currentUser.get(principal));
                ra.addFlashAttribute("success",
                        "Hackathon soumis : il sera visible dans le catalogue après validation par un administrateur.");
                return "redirect:/organizer/hackathons";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return formPage(model, null);
    }

    // ---------- modification ----------
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        Hackathon h = hackathonService.getOwned(id, currentUser.get(principal));
        model.addAttribute("form", HackathonForm.from(h));
        return formPage(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") HackathonForm form,
                         BindingResult result, Principal principal, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                hackathonService.update(id, form, currentUser.get(principal));
                ra.addFlashAttribute("success", "Hackathon modifié.");
                return "redirect:/organizer/hackathons";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return formPage(model, id);
    }

    // ---------- annulation / suppression ----------
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        hackathonService.cancel(id, currentUser.get(principal));
        ra.addFlashAttribute("success", "Hackathon annulé.");
        return "redirect:/organizer/hackathons";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        hackathonService.delete(id, currentUser.get(principal));
        ra.addFlashAttribute("success", "Hackathon supprimé.");
        return "redirect:/organizer/hackathons";
    }

    // ---------- inscrits ----------
    @GetMapping("/{id}/registrants")
    public String registrants(@PathVariable Long id, Principal principal, Model model) {
        Hackathon h = hackathonService.getOwned(id, currentUser.get(principal));
        model.addAttribute("hackathon", h);
        model.addAttribute("registrants", registrantService.registrants(id));
        model.addAttribute("confirmedCount", hackathonService.confirmedCount(id));
        return "organizer/registrants";
    }

    @GetMapping("/{id}/registrants.csv")
    public ResponseEntity<byte[]> registrantsCsv(@PathVariable Long id, Principal principal) {
        hackathonService.getOwned(id, currentUser.get(principal));
        byte[] csv = registrantService.toCsv(registrantService.registrants(id));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inscrits-hackathon-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv);
    }

    // ---------- prix et sponsors ----------
    @GetMapping("/{id}/extras")
    public String extras(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("hackathon", hackathonService.getOwned(id, currentUser.get(principal)));
        model.addAttribute("allSponsors", sponsorService.findAll());
        model.addAttribute("prizeForm", new PrizeForm());
        model.addAttribute("sponsorForm", new SponsorForm());
        return "organizer/extras";
    }

    @PostMapping("/{id}/prizes")
    public String addPrize(@PathVariable Long id, @Valid @ModelAttribute PrizeForm form, BindingResult result,
                           Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getFieldError().getDefaultMessage());
        } else {
            prizeService.add(id, form, currentUser.get(principal));
            ra.addFlashAttribute("success", "Prix ajouté.");
        }
        return "redirect:/organizer/hackathons/" + id + "/extras";
    }

    @PostMapping("/{id}/prizes/{prizeId}/delete")
    public String removePrize(@PathVariable Long id, @PathVariable Long prizeId,
                              Principal principal, RedirectAttributes ra) {
        prizeService.remove(id, prizeId, currentUser.get(principal));
        ra.addFlashAttribute("success", "Prix supprimé.");
        return "redirect:/organizer/hackathons/" + id + "/extras";
    }

    @PostMapping("/{id}/sponsors/add")
    public String addSponsor(@PathVariable Long id, @RequestParam Long sponsorId,
                             Principal principal, RedirectAttributes ra) {
        sponsorService.attach(id, sponsorId, currentUser.get(principal));
        ra.addFlashAttribute("success", "Sponsor ajouté.");
        return "redirect:/organizer/hackathons/" + id + "/extras";
    }

    @PostMapping("/{id}/sponsors/new")
    public String newSponsor(@PathVariable Long id, @Valid @ModelAttribute SponsorForm form, BindingResult result,
                             Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getFieldError().getDefaultMessage());
        } else {
            sponsorService.createAndAttach(id, form, currentUser.get(principal));
            ra.addFlashAttribute("success", "Sponsor créé et ajouté.");
        }
        return "redirect:/organizer/hackathons/" + id + "/extras";
    }

    @PostMapping("/{id}/sponsors/{sponsorId}/remove")
    public String removeSponsor(@PathVariable Long id, @PathVariable Long sponsorId,
                                Principal principal, RedirectAttributes ra) {
        sponsorService.detach(id, sponsorId, currentUser.get(principal));
        ra.addFlashAttribute("success", "Sponsor retiré.");
        return "redirect:/organizer/hackathons/" + id + "/extras";
    }

    // ---------- commun ----------
    private String formPage(Model model, Long editId) {
        model.addAttribute("editId", editId);
        model.addAttribute("organizations", organizationService.findAll());
        model.addAttribute("themes", themeService.findAll());
        model.addAttribute("modes", HackathonMode.values());
        return "organizer/hackathon-form";
    }
}
