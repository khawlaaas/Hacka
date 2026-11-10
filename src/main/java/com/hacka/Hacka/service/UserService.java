package com.hacka.Hacka.service;

import com.hacka.Hacka.dto.ProfileDto;
import com.hacka.Hacka.dto.RegisterDto;
import com.hacka.Hacka.entity.Role;
import com.hacka.Hacka.entity.Skill;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.SkillRepository;
import com.hacka.Hacka.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new BusinessException("Cet email est déjà utilisé.");
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            throw new BusinessException("Les mots de passe ne correspondent pas.");

        User u = new User();
        u.setFirstName(dto.getFirstName());
        u.setLastName(dto.getLastName());
        u.setEmail(dto.getEmail().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setCity(dto.getCity());
        // sécurité : on ne laisse JAMAIS créer un ADMIN par le formulaire
        u.setRole(dto.getRole() == Role.ORGANIZER ? Role.ORGANIZER : Role.PARTICIPANT);
        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }

    @Transactional(readOnly = true)
    public ProfileDto toProfileDto(User u) {
        ProfileDto d = new ProfileDto();
        d.setFirstName(u.getFirstName()); d.setLastName(u.getLastName());
        d.setCity(u.getCity()); d.setBio(u.getBio());
        d.setSkillIds(u.getSkills().stream().map(Skill::getId).toList());
        return d;
    }

    @Transactional
    public void updateProfile(String email, ProfileDto dto) {
        User u = getByEmail(email);
        u.setFirstName(dto.getFirstName());
        u.setLastName(dto.getLastName());
        u.setCity(dto.getCity());
        u.setBio(dto.getBio());
        u.getSkills().clear();
        u.getSkills().addAll(skillRepository.findAllById(dto.getSkillIds()));
        userRepository.save(u);
    }
}
