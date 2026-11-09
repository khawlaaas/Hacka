package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.ReviewForm;
import com.hacka.Hacka.service.CurrentUserResolver;
import com.hacka.Hacka.service.FavoriteService;
import com.hacka.Hacka.service.RecommendationService;
import com.hacka.Hacka.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final CurrentUserResolver currentUser;

    @PostMapping("/hackathons/{id}/favorite")
    public String toggle(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        boolean added = favoriteService.toggle(currentUser.get(principal).getId(), id);
        ra.addFlashAttribute("success", added ? "Ajouté aux favoris." : "Retiré des favoris.");
        return "redirect:/hackathons/" + id;
    }

    @GetMapping("/my/favorites")
    public String list(Principal principal, Model model) {
        model.addAttribute("favorites", favoriteService.list(currentUser.get(principal).getId()));
        return "participation/favorites";
    }
}



