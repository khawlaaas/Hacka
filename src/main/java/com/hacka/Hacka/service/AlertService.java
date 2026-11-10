package com.hacka.Hacka.service;

import com.hacka.Hacka.dto.AlertDto;
import com.hacka.Hacka.entity.Alert;
import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.AlertRepository;
import com.hacka.Hacka.repository.ThemeRepository;
import com.hacka.Hacka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final ThemeRepository themeRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Alert> listFor(String email) {
        return alertRepository.findByUserOrderByCreatedAtDesc(getUser(email));
    }

    @Transactional
    public void create(String email, AlertDto dto) {
        boolean empty = (dto.getKeywords() == null || dto.getKeywords().isBlank())
                && (dto.getCity() == null || dto.getCity().isBlank())
                && dto.getMode() == null && dto.getThemeId() == null;
        if (empty) throw new BusinessException("Renseignez au moins un critère pour l'alerte.");
        Alert a = new Alert();
        a.setUser(getUser(email));
        a.setKeywords(dto.getKeywords() == null ? null : dto.getKeywords().trim());
        a.setCity(dto.getCity() == null ? null : dto.getCity().trim());
        a.setMode(dto.getMode());
        if (dto.getThemeId() != null)
            a.setTheme(themeRepository.findById(dto.getThemeId())
                    .orElseThrow(() -> new BusinessException("Thème introuvable.")));
        alertRepository.save(a);
    }

    @Transactional
    public void toggle(String email, Long id) { own(email, id).setActive(!own(email, id).isActive()); }

    @Transactional
    public void delete(String email, Long id) { alertRepository.delete(own(email, id)); }

    /** Appelé quand un hackathon est validé : prévient les utilisateurs dont une alerte correspond. */
    @Transactional
    public void notifyMatchingAlerts(Hackathon h) {
        String text = ((h.getTitle() == null ? "" : h.getTitle()) + " "
                + (h.getDescription() == null ? "" : h.getDescription())).toLowerCase();
        String city = h.getLocation() != null ? h.getLocation().getCity() : null;
        for (Alert a : alertRepository.findByActiveTrue()) {
            if (a.getMode() != null && a.getMode() != h.getMode()) continue;
            if (a.getCity() != null && !a.getCity().isBlank()
                    && (city == null || !a.getCity().equalsIgnoreCase(city))) continue;
            if (a.getTheme() != null && h.getThemes().stream()
                    .noneMatch(t -> t.getId().equals(a.getTheme().getId()))) continue;
            if (a.getKeywords() != null && !a.getKeywords().isBlank()) {
                boolean any = Arrays.stream(a.getKeywords().toLowerCase().split("[,\\s]+"))
                        .filter(k -> !k.isBlank()).anyMatch(text::contains);
                if (!any) continue;
            }
            notificationService.notify(a.getUser(),
                    "Nouveau hackathon correspondant à votre alerte : " + h.getTitle());
        }
    }

    private Alert own(String email, Long id) {
        Alert a = alertRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Alerte introuvable."));
        if (!a.getUser().getEmail().equals(email))
            throw new BusinessException("Cette alerte ne vous appartient pas.");
        return a;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }
}

