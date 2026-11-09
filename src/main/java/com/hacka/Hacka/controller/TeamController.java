package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.TeamForm;
import com.hacka.Hacka.entity.Team;
import com.hacka.Hacka.service.CurrentUserResolver;
import com.hacka.Hacka.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final CurrentUserResolver currentUser;

    @GetMapping("/hackathons/{hid}/teams")
    public String list(@PathVariable Long hid, Principal principal, Model model) {
        Long me = currentUser.get(principal).getId();
        Optional<Team> myTeam = teamService.findTeamOf(me, hid);
        model.addAttribute("hackathon", teamService.getHackathon(hid));
        model.addAttribute("teams", teamService.teamsOf(hid));
        model.addAttribute("myTeam", myTeam.orElse(null));
        model.addAttribute("amLeader", myTeam.map(t -> teamService.isLeader(t, me)).orElse(false));
        model.addAttribute("finder", teamService.teamFinder(hid));   // Team Finder
        return "participation/teams";
    }

    @GetMapping("/hackathons/{hid}/teams/new")
    public String form(@PathVariable Long hid, Model model) {
        model.addAttribute("teamForm", new TeamForm());
        model.addAttribute("hackathon", teamService.getHackathon(hid));
        return "participation/team-form";
    }

    @PostMapping("/hackathons/{hid}/teams")
    public String create(@PathVariable Long hid, @Valid @ModelAttribute TeamForm teamForm,
                         BindingResult result, Principal principal, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("hackathon", teamService.getHackathon(hid));
            return "participation/team-form";                  // réaffiche avec les erreurs
        }
        teamService.createTeam(currentUser.get(principal).getId(), hid, teamForm);
        ra.addFlashAttribute("success", "Équipe créée !");
        return "redirect:/hackathons/" + hid + "/teams";
    }

    @PostMapping("/teams/{id}/apply")
    public String apply(@PathVariable Long id, @RequestParam(required = false) String message,
                        Principal principal, RedirectAttributes ra, HttpServletRequest req) {
        teamService.apply(currentUser.get(principal).getId(), id, message);
        ra.addFlashAttribute("success", "Candidature envoyée.");
        return "redirect:" + req.getHeader("Referer");
    }

    @PostMapping("/teams/{id}/invite")
    public String invite(@PathVariable Long id, @RequestParam Long userId,
                         Principal principal, RedirectAttributes ra, HttpServletRequest req) {
        teamService.invite(currentUser.get(principal).getId(), id, userId);
        ra.addFlashAttribute("success", "Invitation envoyée.");
        return "redirect:" + req.getHeader("Referer");
    }

    @GetMapping("/my/requests")
    public String requests(Principal principal, Model model) {
        Long me = currentUser.get(principal).getId();
        model.addAttribute("invitations", teamService.invitationsFor(me));
        model.addAttribute("applications", teamService.applicationsFor(me));
        return "participation/requests";
    }

    @PostMapping("/requests/{id}/accept")
    public String accept(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        teamService.respond(id, currentUser.get(principal).getId(), true);
        ra.addFlashAttribute("success", "Demande acceptée.");
        return "redirect:/my/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String reject(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        teamService.respond(id, currentUser.get(principal).getId(), false);
        ra.addFlashAttribute("success", "Demande refusée.");
        return "redirect:/my/requests";
    }

    @PostMapping("/teams/{id}/leave")
    public String leave(@PathVariable Long id, Principal principal, RedirectAttributes ra,
                        HttpServletRequest req) {
        teamService.leave(currentUser.get(principal).getId(), id);
        ra.addFlashAttribute("success", "Vous avez quitté l'équipe.");
        return "redirect:/my/registrations";
    }
}