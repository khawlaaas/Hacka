package com.hacka.Hacka.service;

import com.hacka.Hacka.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver {
    private final UserRepository userRepository;



    public User get(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }
}