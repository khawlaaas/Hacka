package com.hacka.Hacka.service;

import com.hacka.Hacka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.hacka.Hacka.entity.User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email inconnu"));
        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole().name())     // ajoute automatiquement ROLE_
                .disabled(!u.isEnabled())
                .build();
    }
}