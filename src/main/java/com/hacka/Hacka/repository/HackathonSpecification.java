package com.hacka.Hacka.repository;

import com.hacka.Hacka.dto.HackathonFilter;
import com.hacka.Hacka.entity.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Recherche multicritères du catalogue (Specification + Pageable). */
public final class HackathonSpecification {

    /** Statuts affichés par défaut : les hackathons terminés s'obtiennent via le filtre de statut. */
    private static final Set<HackathonStatus> DEFAULT_STATUSES =
            EnumSet.of(HackathonStatus.VALIDATED, HackathonStatus.REGISTRATION_OPEN, HackathonStatus.ONGOING);

    private HackathonSpecification() {}

    public static Specification<Hackathon> from(HackathonFilter f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // Statut : toujours restreint aux statuts publics (RG-08)
            if (f.getStatus() != null && f.getStatus().isPublic()) {
                ps.add(cb.equal(root.get("status"), f.getStatus()));
            } else {
                ps.add(root.get("status").in(DEFAULT_STATUSES));
            }

            if (hasText(f.getKeyword())) {
                String like = "%" + f.getKeyword().trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }

            if (hasText(f.getCity())) {
                Join<Hackathon, Location> loc = root.join("location", JoinType.LEFT);
                ps.add(cb.like(cb.lower(loc.get("city")), "%" + f.getCity().trim().toLowerCase() + "%"));
            }

            if (f.getMode() != null) {
                ps.add(cb.equal(root.get("mode"), f.getMode()));
            }

            if (f.getThemeId() != null) {
                Join<Hackathon, Theme> theme = root.join("themes", JoinType.INNER);
                ps.add(cb.equal(theme.get("id"), f.getThemeId()));
                query.distinct(true);
            }

            if (f.getFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("startDate"), f.getFrom().atStartOfDay()));
            }
            if (f.getTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("startDate"), f.getTo().atTime(LocalTime.MAX)));
            }

            if (f.isOnlyAvailable()) {
                Subquery<Long> taken = query.subquery(Long.class);
                Root<Registration> r = taken.from(Registration.class);
                taken.select(cb.count(r)).where(
                        cb.equal(r.get("hackathon"), root),
                        cb.equal(r.get("status"), RegistrationStatus.CONFIRMED));
                ps.add(cb.or(
                        cb.isNull(root.get("maxParticipants")),
                        cb.gt(root.<Number>get("maxParticipants"), taken)));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
