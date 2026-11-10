package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Role;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> all() { return userRepository.findAll(); }

    @Transactional
    public void changeRole(Long id, Role role, String currentEmail) {
        User u = userRepository.findById(id).orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        if (u.getEmail().equals(currentEmail))
            throw new BusinessException("Vous ne pouvez pas modifier votre propre rôle.");
        u.setRole(role);
    }

    @Transactional
    public void toggleEnabled(Long id, String currentEmail) {
        User u = userRepository.findById(id).orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        if (u.getEmail().equals(currentEmail))
            throw new BusinessException("Vous ne pouvez pas bloquer votre propre compte.");
        u.setEnabled(!u.isEnabled());
    }
}
