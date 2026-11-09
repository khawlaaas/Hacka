package com.hacka.Hacka.service;

import com.hacka.Hacka.dto.HackathonFilter;
import com.hacka.Hacka.dto.HackathonForm;
import com.hacka.Hacka.entity.*;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Logique métier du catalogue : CRUD, recherche, machine à états, workflow de validation.
 * Les méthodes validate/reject/getProposed sont destinées à l'espace admin (module A).
 */
@Service
@RequiredArgsConstructor
public class HackathonService {

    private static final Set<HackathonStatus> UPCOMING =
            EnumSet.of(HackathonStatus.VALIDATED, HackathonStatus.REGISTRATION_OPEN, HackathonStatus.ONGOING);
    private static final Set<HackathonStatus> NOT_DUPLICATE_CANDIDATES =
            EnumSet.of(HackathonStatus.REJECTED, HackathonStatus.CANCELLED);

    private final HackathonRepository hackathonRepository;
    private final OrganizationRepository organizationRepository;
    private final ThemeRepository themeRepository;
    private final LocationRepository locationRepository;
    private final RegistrationRepository registrationRepository;   // module C
    private final DuplicateDetector duplicateDetector;

    // ======================= lecture =======================

    @Transactional(readOnly = true)
    public Page<Hackathon> search(HackathonFilter filter, Pageable pageable) {
        return hackathonRepository.findAll(HackathonSpecification.from(filter), pageable);
    }

    @Transactional(readOnly = true)
    public Hackathon find(Long id) {
        return hackathonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
    }

    /** Page de détail : PROPOSED et REJECTED ne sont visibles que de leur organisateur et des admins (RG-08). */
    @Transactional(readOnly = true)
    public Hackathon getViewable(Long id, User viewer) {
        Hackathon h = find(id);
        boolean hidden = h.getStatus() == HackathonStatus.PROPOSED || h.getStatus() == HackathonStatus.REJECTED;
        if (hidden && (viewer == null || !canManage(h, viewer))) {
            throw new BusinessException("Ce hackathon n'est pas (encore) publié.");
        }
        return h;
    }

    @Transactional(readOnly = true)
    public Hackathon getOwned(Long id, User user) {
        Hackathon h = find(id);
        if (!canManage(h, user)) {
            throw new BusinessException("Vous n'êtes pas l'organisateur de ce hackathon.");
        }
        return h;
    }

    @Transactional(readOnly = true)
    public List<Hackathon> mine(Long userId) {
        return hackathonRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
    }

    /** File d'attente de validation pour l'espace admin (module A). */
    @Transactional(readOnly = true)
    public List<Hackathon> getProposed() {
        return hackathonRepository.findByStatusOrderByCreatedAtAsc(HackathonStatus.PROPOSED);
    }

    @Transactional(readOnly = true)
    public long confirmedCount(Long hackathonId) {
        return registrationRepository.countByHackathonIdAndStatus(hackathonId, RegistrationStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public List<Hackathon> upcomingForCalendar() {
        return hackathonRepository.findByStatusInAndEndDateGreaterThanEqualOrderByStartDateAsc(
                UPCOMING, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Hackathon> openForHome() {
        return hackathonRepository.findTop6ByStatusOrderByStartDateAsc(HackathonStatus.REGISTRATION_OPEN);
    }

    @Transactional(readOnly = true)
    public List<Hackathon> closingSoon() {
        LocalDateTime now = LocalDateTime.now();
        return hackathonRepository.findClosingSoon(HackathonStatus.REGISTRATION_OPEN, now, now.plusDays(7));
    }

    @Transactional(readOnly = true)
    public long count(HackathonStatus status) {
        return hackathonRepository.countByStatus(status);
    }

    // ======================= écriture =======================

    /** RG-08 : un hackathon publié est créé au statut PROPOSED. */
    @Transactional
    public Hackathon create(HackathonForm f, User creator) {
        if (f.getStartDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La date de début doit être dans le futur.");
        }
        Hackathon h = new Hackathon();
        h.setCreatedBy(creator);
        h.setStatus(HackathonStatus.PROPOSED);
        apply(f, h);
        return hackathonRepository.save(h);
    }

    @Transactional
    public Hackathon update(Long id, HackathonForm f, User user) {
        Hackathon h = getOwned(id, user);
        if (!h.getStatus().isEditable()) {
            throw new BusinessException("Ce hackathon ne peut plus être modifié (statut : "
                    + h.getStatus().getLabel() + ").");
        }
        if (f.getMaxParticipants() != null && f.getMaxParticipants() < confirmedCount(id)) {
            throw new BusinessException("Impossible de descendre sous le nombre d'inscrits confirmés ("
                    + confirmedCount(id) + ").");
        }
        apply(f, h);
        return h;
    }

    @Transactional
    public void cancel(Long id, User user) {
        transition(getOwned(id, user), HackathonStatus.CANCELLED);
    }

    /** Suppression définitive : uniquement un hackathon jamais publié. Sinon, on l'annule. */
    @Transactional
    public void delete(Long id, User user) {
        Hackathon h = getOwned(id, user);
        if (!h.getStatus().isDeletable()) {
            throw new BusinessException("Un hackathon déjà publié ne se supprime pas : annulez-le.");
        }
        hackathonRepository.delete(h);
    }

    // ======================= workflow admin (appelé par le module A) =======================

    @Transactional
    public void validate(Long id) {
        Hackathon h = find(id);
        transition(h, HackathonStatus.VALIDATED);
        advance(h, LocalDateTime.now());   // ouverture automatique des inscriptions
    }

    @Transactional
    public void reject(Long id) {
        transition(find(id), HackathonStatus.REJECTED);
    }

    // ======================= statuts automatiques (@Scheduled) =======================

    /** Fait évoluer les statuts selon les dates. Retourne le nombre de hackathons modifiés. */
    @Transactional
    public int refreshStatuses() {
        LocalDateTime now = LocalDateTime.now();
        int changed = 0;
        for (Hackathon h : hackathonRepository.findByStatusIn(
                EnumSet.of(HackathonStatus.VALIDATED, HackathonStatus.REGISTRATION_OPEN, HackathonStatus.ONGOING))) {
            if (advance(h, now)) changed++;
        }
        return changed;
    }

    private boolean advance(Hackathon h, LocalDateTime now) {
        boolean any = false;
        boolean moved;
        do {
            moved = false;
            switch (h.getStatus()) {
                case VALIDATED -> { h.setStatus(HackathonStatus.REGISTRATION_OPEN); moved = true; }
                case REGISTRATION_OPEN -> {
                    if (!now.isBefore(h.getStartDate())) { h.setStatus(HackathonStatus.ONGOING); moved = true; }
                }
                case ONGOING -> {
                    if (!now.isBefore(h.getEndDate())) { h.setStatus(HackathonStatus.FINISHED); moved = true; }
                }
                default -> { }
            }
            any |= moved;
        } while (moved);
        return any;
    }

    // ======================= doublons =======================

    @Transactional(readOnly = true)
    public List<Hackathon> findPossibleDuplicates(String title, LocalDateTime start) {
        return hackathonRepository
                .findNearStart(NOT_DUPLICATE_CANDIDATES, start.minusDays(3), start.plusDays(3))
                .stream()
                .filter(h -> duplicateDetector.isSimilar(title, h.getTitle()))
                .toList();
    }

    // ======================= utilitaires =======================

    public boolean canManage(Hackathon h, User user) {
        return user.getRole() == Role.ADMIN || h.getCreatedBy().getId().equals(user.getId());
    }

    public static String formatDate(LocalDateTime d) {
        return d == null ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void transition(Hackathon h, HackathonStatus next) {
        if (!h.getStatus().canTransitionTo(next)) {
            throw new BusinessException("Transition impossible : " + h.getStatus().getLabel()
                    + " → " + next.getLabel() + ".");
        }
        h.setStatus(next);
    }

    private void apply(HackathonForm f, Hackathon h) {
        h.setTitle(f.getTitle().trim());
        h.setDescription(f.getDescription());
        h.setStartDate(f.getStartDate());
        h.setEndDate(f.getEndDate());
        h.setRegistrationDeadline(f.getRegistrationDeadline());
        h.setMode(f.getMode());
        h.setMaxParticipants(f.getMaxParticipants());
        h.setExternalLink(f.getExternalLink() == null || f.getExternalLink().isBlank() ? null : f.getExternalLink().trim());

        h.setOrganization(organizationRepository.findById(f.getOrganizationId())
                .orElseThrow(() -> new BusinessException("Organisation introuvable.")));

        Set<Theme> themes = new HashSet<>(themeRepository.findAllById(f.getThemeIds()));
        if (themes.isEmpty()) throw new BusinessException("Choisissez au moins un thème valide.");
        h.setThemes(themes);

        if (f.getMode() == HackathonMode.ONLINE) {
            h.setLocation(null);
        } else {
            String city = f.getLocationCity().trim();
            String address = f.getLocationAddress() == null ? "" : f.getLocationAddress().trim();
            h.setLocation(locationRepository.findFirstByCityIgnoreCaseAndAddressIgnoreCase(city, address)
                    .orElseGet(() -> locationRepository.save(new Location(city, address))));
        }
    }
}
