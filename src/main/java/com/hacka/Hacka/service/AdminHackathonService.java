package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.entity.HackathonStatus;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.HackathonRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminHackathonService {
    private final HackathonRepository hackathonRepository;
    private final NotificationService notificationService;
    private final AlertService alertService;

    @Transactional(readOnly = true)
    public List<Hackathon> pending() { return hackathonRepository.findByStatus(HackathonStatus.PROPOSED); }

    @Transactional
    public void validate(Long id) {
        Hackathon h = get(id);
        h.setStatus(HackathonStatus.VALIDATED);
        notificationService.alertMatchingUsers(h);   // correspondance compétences/thèmes
        alertService.notifyMatchingAlerts(h);        // alertes enregistrées (BF-28)
        notifyOrganizer(h, "Votre hackathon « " + h.getTitle()
                + " » a été validé et sera bientôt visible dans le catalogue.");
    }

    @Transactional
    public void reject(Long id, String reason) {
        Hackathon h = get(id);
        h.setStatus(HackathonStatus.REJECTED);
        String motif = (reason == null || reason.isBlank()) ? "" : " Motif : " + reason.trim();
        notifyOrganizer(h, "Votre hackathon « " + h.getTitle() + " » a été refusé." + motif);
    }

    private void notifyOrganizer(Hackathon h, String message) {
        if (h.getCreatedBy() != null) notificationService.notify(h.getCreatedBy(), message);
    }

    private Hackathon get(Long id) {
        Hackathon h = hackathonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
        if (h.getStatus() != HackathonStatus.PROPOSED)
            throw new BusinessException("Seul un hackathon proposé peut être validé ou refusé.");
        return h;
    }
}
