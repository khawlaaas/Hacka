package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.HackathonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Module A : le changement de statut est délégué à HackathonService (module B). */
@Service
@RequiredArgsConstructor
public class AdminHackathonService {
    private final HackathonService hackathonService;        // service de B
    private final HackathonRepository hackathonRepository;
    private final NotificationService notificationService;
    private final AlertService alertService;

    @Transactional(readOnly = true)
    public List<Hackathon> pending() {
        return hackathonService.getProposed();
    }

    @Transactional
    public void validate(Long id) {
        hackathonService.validate(id);                       // règles de statut chez B
        Hackathon h = find(id);
        notificationService.alertMatchingUsers(h);           // compétences / thèmes
        alertService.notifyMatchingAlerts(h);                // alertes enregistrées
        notifyOrganizer(h, "Votre hackathon « " + h.getTitle()
                + " » a été validé et sera bientôt visible dans le catalogue.");
    }

    @Transactional
    public void reject(Long id, String reason) {
        hackathonService.reject(id);                         // règles de statut chez B
        Hackathon h = find(id);
        String motif = (reason == null || reason.isBlank()) ? "" : " Motif : " + reason.trim();
        notifyOrganizer(h, "Votre hackathon « " + h.getTitle() + " » a été refusé." + motif);
    }

    private Hackathon find(Long id) {
        return hackathonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
    }

    private void notifyOrganizer(Hackathon h, String message) {
        if (h.getCreatedBy() != null) notificationService.notify(h.getCreatedBy(), message);
    }
}