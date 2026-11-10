package com.hacka.Hacka.controller;

import com.hacka.Hacka.entity.HackathonStatus;
import com.hacka.Hacka.service.HackathonService;
import com.hacka.Hacka.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final HackathonService hackathonService;
    private final ThemeService themeService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("openCount", hackathonService.count(HackathonStatus.REGISTRATION_OPEN));
        model.addAttribute("closingSoon", hackathonService.closingSoon());
        model.addAttribute("upcoming", hackathonService.openForHome());
        model.addAttribute("themes", themeService.findAll());
        return "hackathons/home";
    }
}
