package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.HackathonStatus;
import com.hacka.Hacka.entity.Report;
import com.hacka.Hacka.entity.ReportStatus;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.HackathonRepository;
import com.hacka.Hacka.repository.ReportRepository;
import com.hacka.Hacka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;

    @Transactional
    public void create(String email, Long hackathonId, String reason) {
        if (reason == null || reason.isBlank())
            throw new BusinessException("Le motif du signalement est obligatoire.");
        if (reason.length() > 500)
            throw new BusinessException("Le motif ne doit pas dépasser 500 caractères.");
        Report r = new Report();
        r.setUser(userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable.")));
        r.setHackathon(hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable.")));
        r.setReason(reason.trim());
        reportRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<Report> all() { return reportRepository.findAllByOrderByCreatedAtDesc(); }

    @Transactional
    public void resolve(Long id) { get(id).setStatus(ReportStatus.RESOLVED); }

    /** Clôture + retrait du hackathon (statut CANCELLED, jamais de suppression : RESTRICT). */
    @Transactional
    public void resolveAndCancel(Long id) {
        Report r = get(id);
        r.setStatus(ReportStatus.RESOLVED);
        r.getHackathon().setStatus(HackathonStatus.CANCELLED);
    }

    private Report get(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Signalement introuvable."));
    }
}
