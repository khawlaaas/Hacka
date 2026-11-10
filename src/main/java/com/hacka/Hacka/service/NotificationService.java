package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.entity.HackathonStatus;
import com.hacka.Hacka.entity.Notification;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.repository.HackathonRepository;
import com.hacka.Hacka.repository.NotificationRepository;
import com.hacka.Hacka.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;

    /** Notification en base + "envoi d'email" simulé dans les logs. */
    @Transactional
    public void notify(User u, String message) {
        if (notificationRepository.existsByUserAndMessage(u, message)) return;   // évite les doublons
        Notification n = new Notification();
        n.setUser(u); n.setMessage(message);
        notificationRepository.save(n);
        log.info("[EMAIL SIMULÉ] à {} : {}", u.getEmail(), message);
    }

    /** Alerte les utilisateurs dont une compétence correspond à un thème du hackathon. */
    @Transactional
    public void alertMatchingUsers(Hackathon h) {
        Set<String> themes = h.getThemes().stream().map(t -> t.getName().toLowerCase()).collect(Collectors.toSet());
        for (User u : userRepository.findAll()) {
            boolean match = u.getSkills().stream().anyMatch(s -> themes.contains(s.getName().toLowerCase()));
            if (match) notify(u, "Nouveau hackathon qui correspond à votre profil : " + h.getTitle());
        }
    }

    /** Chaque matin à 8h : rappel des deadlines dans les 48 prochaines heures. */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void deadlineReminders() {
        LocalDateTime now = LocalDateTime.now();
        for (Hackathon h : hackathonRepository.findByStatus(HackathonStatus.REGISTRATION_OPEN)) {
            if (h.getRegistrationDeadline() != null
                    && h.getRegistrationDeadline().isAfter(now)
                    && h.getRegistrationDeadline().isBefore(now.plusHours(48))) {
                alertMatchingUsers(h);   // à adapter : message "deadline proche"
            }
        }
    }
    // + lecture : list(User), unreadCount(User), markAllRead(User)
}
