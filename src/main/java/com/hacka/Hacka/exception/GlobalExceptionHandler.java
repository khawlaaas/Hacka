package com.hacka.Hacka.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;

// exception/GlobalExceptionHandler.java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());   // même nom que dans le fragment "flash" de C
        String referer = req.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(Exception.class)
    public String handleOther(Exception ex, Model model) {
        model.addAttribute("message", "Une erreur inattendue est survenue.");
        return "error/500";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void rethrow(AccessDeniedException ex) throws AccessDeniedException { throw ex; }
}