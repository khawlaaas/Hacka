package com.hacka.Hacka.config;

import com.hacka.Hacka.entity.Role;
import com.hacka.Hacka.entity.Skill;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.repository.SkillRepository;
import com.hacka.Hacka.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        for (String name : List.of("IA", "Web", "Cybersécurité", "Mobile", "Data", "Design", "Cloud", "IoT")) {
            if (!skillRepository.existsByNameIgnoreCase(name)) skillRepository.save(new Skill(name));
        }
        createIfAbsent("participant@hacka.ma", "Sara", "Benani", Role.PARTICIPANT, "Casablanca");
        createIfAbsent("organizer@hacka.ma", "Youssef", "Alaoui", Role.ORGANIZER, "Rabat");
        createIfAbsent("admin@hacka.ma", "Admin", "Hacka", Role.ADMIN, "Casablanca");
    }

    private void createIfAbsent(String email, String fn, String ln, Role role, String city) {
        if (userRepository.existsByEmail(email)) return;
        User u = new User();
        u.setEmail(email); u.setFirstName(fn); u.setLastName(ln);
        u.setRole(role); u.setCity(city);
        u.setPassword(passwordEncoder.encode("Password123"));
        userRepository.save(u);
    }
}
