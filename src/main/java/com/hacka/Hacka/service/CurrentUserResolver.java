package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.UserRepository;
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