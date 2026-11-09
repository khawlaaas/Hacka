package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.HackathonFilter;
import com.hacka.Hacka.entity.*;
import com.hacka.Hacka.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/** Pages publiques du catalogue : liste, détail, calendrier. */
@Controller
@RequiredArgsConstructor
public class HackathonController {
    private static final int PAGE_SIZE = 9;

    private final HackathonService hackathonService;
    private final ThemeService themeService;
    private final CalendarService calendarService;
    private final CurrentUserResolver currentUser;
    // services du module C, utilisés pour la page de détail (contrat section 10 des règles)
    private final RegistrationService registrationService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    @GetMapping("/hackathons")
    public String list(@ModelAttribute("filter") HackathonFilter filter,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Hackathon> result = hackathonService.search(filter,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("startDate").ascending()));
        model.addAttribute("page", result);
        model.addAttribute("themes", themeService.findAll());
        model.addAttribute("modes", HackathonMode.values());
        model.addAttribute("statuses", List.of(HackathonStatus.REGISTRATION_OPEN,
                HackathonStatus.ONGOING, HackathonStatus.FINISHED));
        return "hackathons/list";
    }

    @GetMapping("/hackathons/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        User viewer = principal != null ? currentUser.get(principal) : null;
        Hackathon h = hackathonService.getViewable(id, viewer);

        boolean registered = false;
        boolean favorite = false;
        if (viewer != null) {
            registered = registrationService.isRegistered(viewer.getId(), id);
            favorite = favoriteService.isFavorite(viewer.getId(), id);
        }
        long confirmed = hackathonService.confirmedCount(id);

        model.addAttribute("hackathon", h);
        model.addAttribute("registered", registered);
        model.addAttribute("favorite", favorite);
        model.addAttribute("reviews", reviewService.reviewsOf(id));
        model.addAttribute("avgRating", reviewService.average(id));
        model.addAttribute("confirmedCount", confirmed);
        model.addAttribute("remainingPlaces",
                h.getMaxParticipants() == null ? null : Math.max(0, h.getMaxParticipants() - confirmed));
        model.addAttribute("canManage", viewer != null && hackathonService.canManage(h, viewer));
        return "hackathons/detail";
    }

    @GetMapping("/hackathons/calendar")
    public String calendar(Model model) {
        model.addAttribute("months", calendarService.groupByMonth(hackathonService.upcomingForCalendar()));
        return "hackathons/calendar";
    }

    @GetMapping("/hackathons/calendar.ics")
    public ResponseEntity<byte[]> calendarIcs(HttpServletRequest request) {
        return ics(hackathonService.upcomingForCalendar(), "hacka-hackathons.ics", request);
    }

    @GetMapping("/hackathons/{id}/calendar.ics")
    public ResponseEntity<byte[]> hackathonIcs(@PathVariable Long id, Principal principal,
                                               HttpServletRequest request) {
        User viewer = principal != null ? currentUser.get(principal) : null;
        Hackathon h = hackathonService.getViewable(id, viewer);
        return ics(List.of(h), "hackathon-" + id + ".ics", request);
    }

    private ResponseEntity<byte[]> ics(List<Hackathon> list, String filename, HttpServletRequest request) {
        String base = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/calendar;charset=UTF-8"))
                .body(calendarService.toIcs(list, base));
    }
}
