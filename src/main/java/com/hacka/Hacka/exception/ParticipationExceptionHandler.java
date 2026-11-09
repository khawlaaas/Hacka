package com.hacka.Hacka.exception;

import com.hacka.Hacka.controller.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = {RegistrationController.class, TeamController.class,
        FavoriteController.class, ReviewController.class, RecommendationController.class})
public class ParticipationExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public String handle(BusinessException ex, RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("error", ex.getMessage());
        String back = request.getHeader("Referer");
        return "redirect:" + (back != null ? back : "/");
    }
}