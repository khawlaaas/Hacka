package com.hacka.Hacka.service;

import com.hacka.Hacka.repository.*;

import com.hacka.Hacka.entity.*;

import com.hacka.Hacka.entity.Registration;
import com.hacka.Hacka.entity.RegistrationStatus;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.RegistrationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final TeamService teamService;
    private final BadgeService badgeService;

    @Transactional
    public Registration register(Long userId, Long hackathonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));

        if (hackathon.getStatus() != HackathonStatus.REGISTRATION_OPEN) {
            throw new BusinessException("Les inscriptions ne sont pas ouvertes pour ce hackathon.");
        }
        if (hackathon.getRegistrationDeadline() != null
                && LocalDateTime.now().isAfter(hackathon.getRegistrationDeadline())) {
            throw new BusinessException("La date limite d'inscription est dépassée.");
        }

        Optional<Registration> existing =
                registrationRepository.findByUserIdAndHackathonId(userId, hackathonId);
        if (existing.isPresent() && existing.get().getStatus() != RegistrationStatus.CANCELLED) {
            throw new BusinessException("Vous êtes déjà inscrit à ce hackathon.");
        }

        long taken = registrationRepository
                .countByHackathonIdAndStatus(hackathonId, RegistrationStatus.CONFIRMED);
        if (hackathon.getMaxParticipants() != null && taken >= hackathon.getMaxParticipants()) {
            throw new BusinessException("Ce hackathon est complet.");
        }

        // Après une annulation, on réactive la même ligne (contrainte UNIQUE user + hackathon).
        Registration registration = existing.orElseGet(Registration::new);
        registration.setUser(user);
        registration.setHackathon(hackathon);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.CONFIRMED);
        Registration saved = registrationRepository.save(registration);
        badgeService.evaluate(userId);
        return saved;
    }

    @Transactional
    public void cancel(Long userId, Long hackathonId) {
        Registration registration = registrationRepository.findByUserIdAndHackathonId(userId, hackathonId)
                .filter(r -> r.getStatus() != RegistrationStatus.CANCELLED)
                .orElseThrow(() -> new BusinessException("Vous n'êtes pas inscrit à ce hackathon."));
        registration.setStatus(RegistrationStatus.CANCELLED);

        // Si le participant était dans une équipe de ce hackathon, il la quitte.
        teamService.findTeamOf(userId, hackathonId)
                .ifPresent(team -> teamService.leave(userId, team.getId()));
    }

    public boolean isRegistered(Long userId, Long hackathonId) {
        return registrationRepository.findByUserIdAndHackathonId(userId, hackathonId)
                .map(r -> r.getStatus() == RegistrationStatus.CONFIRMED)
                .orElse(false);
    }

    public List<Registration> history(Long userId) {
        return registrationRepository.findByUserIdOrderByRegistrationDateDesc(userId);
    }
}
