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

    @Transactional
    public Registration register(Long userId, Long hackathonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        Hackathon h = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));

        if (h.getStatus() != HackathonStatus.REGISTRATION_OPEN)
            throw new BusinessException("Les inscriptions ne sont pas ouvertes pour ce hackathon.");
        if (h.getRegistrationDeadline() != null
                && LocalDateTime.now().isAfter(h.getRegistrationDeadline()))
            throw new BusinessException("La date limite d'inscription est dépassée.");

        Optional<Registration> existing =
                registrationRepository.findByUserIdAndHackathonId(userId, hackathonId);
        if (existing.isPresent() && existing.get().getStatus() != RegistrationStatus.CANCELLED)
            throw new BusinessException("Vous êtes déjà inscrit à ce hackathon.");

        long taken = registrationRepository
                .countByHackathonIdAndStatus(hackathonId, RegistrationStatus.CONFIRMED);
        if (h.getMaxParticipants() != null && taken >= h.getMaxParticipants())
            throw new BusinessException("Ce hackathon est complet.");

        // si l'utilisateur avait annulé, on réactive sa ligne (contrainte UNIQUE user+hackathon)
        Registration r = existing.orElseGet(Registration::new);
        r.setUser(user);
        r.setHackathon(h);
        r.setRegistrationDate(LocalDateTime.now());
        r.setStatus(RegistrationStatus.CONFIRMED);
        return registrationRepository.save(r);
    }

    @Transactional
    public void cancel(Long userId, Long hackathonId) {
        Registration r = registrationRepository.findByUserIdAndHackathonId(userId, hackathonId)
                .filter(x -> x.getStatus() != RegistrationStatus.CANCELLED)
                .orElseThrow(() -> new BusinessException("Vous n'êtes pas inscrit à ce hackathon."));
        r.setStatus(RegistrationStatus.CANCELLED);
    }

    public boolean isRegistered(Long userId, Long hackathonId) {
        return registrationRepository.findByUserIdAndHackathonId(userId, hackathonId)
                .map(r -> r.getStatus() == RegistrationStatus.CONFIRMED).orElse(false);
    }

    public List<Registration> history(Long userId) {
        return registrationRepository.findByUserIdOrderByRegistrationDateDesc(userId);
    }
}