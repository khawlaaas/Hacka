package com.hacka.Hacka.controller;

import com.hacka.Hacka.dto.ReviewForm;
import com.hacka.Hacka.service.CurrentUserResolver;
import com.hacka.Hacka.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final CurrentUserResolver currentUser;

    @PostMapping("/hackathons/{id}/reviews")
    public String add(@PathVariable Long id, @Valid @ModelAttribute ReviewForm form,
                      BindingResult result, Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getFieldError().getDefaultMessage());
        } else {
            reviewService.add(currentUser.get(principal).getId(), id, form);
            ra.addFlashAttribute("success", "Merci pour votre avis !");
        }
        return "redirect:/hackathons/" + id;
    }
}