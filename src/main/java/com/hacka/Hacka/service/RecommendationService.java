package com.hacka.Hacka.service;

import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.*;

import com.hacka.Hacka.entity.*;

import com.hacka.Hacka.dto.ScoredHackathon;
import com.hacka.Hacka.entity.RegistrationStatus;
import com.hacka.Hacka.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final RegistrationRepository registrationRepository;

    @Transactional(readOnly = true)
    public List<ScoredHackathon> recommend(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));

        Set<String> skills = user.getSkills().stream()
                .map(s -> s.getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<Long> alreadyRegistered = registrationRepository.findByUserIdOrderByRegistrationDateDesc(userId)
                .stream()
                .filter(r -> r.getStatus() != RegistrationStatus.CANCELLED)
                .map(r -> r.getHackathon().getId())
                .collect(Collectors.toSet());

        return hackathonRepository.findAll().stream()
                .filter(h -> h.getStatus() == HackathonStatus.REGISTRATION_OPEN)
                .filter(h -> !alreadyRegistered.contains(h.getId()))
                .map(h -> {
                    Set<String> themes = h.getThemes().stream()
                            .map(t -> t.getName().toLowerCase())
                            .collect(Collectors.toSet());
                    return new ScoredHackathon(h, jaccard(skills, themes));
                })
                .filter(s -> s.getScore() > 0)
                .sorted(Comparator.comparingDouble(ScoredHackathon::getScore).reversed())
                .limit(limit)
                .toList();
    }

    static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }
}
