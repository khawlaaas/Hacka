package com.hacka.Hacka.exception;

import com.hacka.Hacka.controller.HackathonController;
import com.hacka.Hacka.controller.HomeController;
import com.hacka.Hacka.controller.OrganizationController;
import com.hacka.Hacka.controller.OrganizerHackathonController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Erreurs métier du module catalogue : message flash + redirection. */
@ControllerAdvice(assignableTypes = {HomeController.class, HackathonController.class,
        OrganizerHackathonController.class, OrganizationController.class})
public class CatalogExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handle(BusinessException ex, RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("error", ex.getMessage());
        // GET : on ne renvoie jamais vers le Referer (risque de boucle de redirection)
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return request.getRequestURI().startsWith("/organizer")
                    ? "redirect:/organizer/hackathons" : "redirect:/hackathons";
        }
        String back = request.getHeader("Referer");
        return "redirect:" + (back != null ? back : "/organizer/hackathons");
    }
}
