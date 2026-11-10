package com.hacka.Hacka.service;


import com.hacka.Hacka.entity.*;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Notification en base + email simulé dans les logs. */
    @Transactional
    public void notify(User u, String message) {
        if (notificationRepository.existsByUserAndMessage(u, message)) return;
        Notification n = new Notification();
        n.setUser(u);
        n.setMessage(message);
        notificationRepository.save(n);
        log.info("[EMAIL SIMULÉ] à {} : {}", u.getEmail(), message);
    }

    /** Appelé par AdminHackathonService.validate(). */
    @Transactional
    public void alertMatchingUsers(Hackathon h) {
        notifyMatching(h, "Nouveau hackathon qui correspond à votre profil : " + h.getTitle());
    }

    private void notifyMatching(Hackathon h, String message) {
        Set<String> themes = h.getThemes().stream()
                .map(t -> t.getName().toLowerCase())
                .collect(Collectors.toSet());
        for (User u : userRepository.findAll()) {
            boolean match = u.getSkills().stream()
                    .anyMatch(s -> themes.contains(s.getName().toLowerCase()));
            if (match) notify(u, message);
        }
    }

    /** Chaque jour à 8h : deadlines dans les 48 prochaines heures. */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void deadlineReminders() {
        LocalDateTime now = LocalDateTime.now();
        for (Hackathon h : hackathonRepository.findByStatus(HackathonStatus.REGISTRATION_OPEN)) {
            LocalDateTime d = h.getRegistrationDeadline();
            if (d != null && d.isAfter(now) && d.isBefore(now.plusHours(48))) {
                notifyMatching(h, "Dernière ligne droite : les inscriptions à « "
                        + h.getTitle() + " » se terminent le " + d.format(FMT) + ".");
            }
        }
    }

    // ----- lecture, utilisée par le controller et le badge -----

    @Transactional(readOnly = true)
    public List<Notification> listFor(String email) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(getUser(email));
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return userRepository.findByEmail(email)
                .map(notificationRepository::countByUserAndReadFalse)
                .orElse(0L);
    }

    @Transactional
    public void markAllRead(String email) {
        for (Notification n : notificationRepository.findByUserOrderByCreatedAtDesc(getUser(email))) {
            n.setRead(true);
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }
}