package com.hacka.Hacka.service;

import com.hacka.Hacka.repository.*;

import com.hacka.Hacka.entity.*;

import com.hacka.Hacka.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public User get(Principal principal) {
        if (principal == null) {
            throw new BusinessException("Vous devez être connecté.");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }
}