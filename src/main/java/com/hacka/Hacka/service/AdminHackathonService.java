package com.hacka.Hacka.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminHackathonService {
    private final HackathonRepository hackathonRepository;
    private final NotificationService notificationService;   // étape 9

    @Transactional(readOnly = true)
    public List<Hackathon> pending() { return hackathonRepository.findByStatus(HackathonStatus.PROPOSED); }

    @Transactional
    public void validate(Long id) {
        Hackathon h = get(id);
        h.setStatus(HackathonStatus.VALIDATED);
        notificationService.alertMatchingUsers(h);
    }

    @Transactional
    public void reject(Long id) {
        Hackathon h = get(id);
        h.setStatus(HackathonStatus.REJECTED);
    }

    private Hackathon get(Long id) {
        Hackathon h = hackathonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
        if (h.getStatus() != HackathonStatus.PROPOSED)
            throw new BusinessException("Seul un hackathon proposé peut être validé ou refusé.");
        return h;
    }
}
