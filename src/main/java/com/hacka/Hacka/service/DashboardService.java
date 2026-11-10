package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Role;
import com.hacka.Hacka.entity.Theme;
import com.hacka.Hacka.repository.HackathonRepository;
import com.hacka.Hacka.repository.RegistrationRepository;
import com.hacka.Hacka.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// service/DashboardService.java
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final RegistrationRepository registrationRepository;

    @Transactional(readOnly = true)
    public Map<String, Long> usersByRole() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (Role r : Role.values()) m.put(r.name(), userRepository.countByRole(r));
        return m;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> hackathonsByStatus() {
        return hackathonRepository.findAll().stream()
                .collect(Collectors.groupingBy(h -> h.getStatus().name(), TreeMap::new, Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> hackathonsByTheme() {
        return hackathonRepository.findAll().stream()
                .flatMap(h -> h.getThemes().stream())
                .collect(Collectors.groupingBy(Theme::getName, TreeMap::new, Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> registrationsByStatus() {
        return registrationRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getStatus().name(), TreeMap::new, Collectors.counting()));
    }
}
