package com.hacka.Hacka.config;

import com.hacka.Hacka.entity.*;
import com.hacka.Hacka.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Données de démo du catalogue : 6 hackathons de statuts variés.
 * Nécessite un utilisateur ORGANIZER (créé par le module A, avec un @Order inférieur à 100).
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {
    private final HackathonRepository hackathonRepository;
    private final OrganizationRepository organizationRepository;
    private final LocationRepository locationRepository;
    private final ThemeRepository themeRepository;
    private final SponsorRepository sponsorRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (hackathonRepository.count() > 0) return;
        User organizer = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ORGANIZER).findFirst().orElse(null);
        if (organizer == null) {
            log.warn("DemoDataLoader : aucun utilisateur ORGANIZER, données de démo non créées.");
            return;
        }

        Organization club = org("Club Innovation (démo)", OrganizationType.CLUB);
        Organization school = org("École d'ingénieurs (démo)", OrganizationType.SCHOOL);
        Organization company = org("TechCorp Maroc (démo)", OrganizationType.COMPANY);

        Sponsor sponsor = new Sponsor();
        sponsor.setName("CloudSoft (démo)");
        sponsor.setWebsite("https://example.com");
        sponsorRepository.save(sponsor);

        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        Hackathon ai = hack("AI Challenge Casablanca", "Résolvez un problème réel avec l'intelligence artificielle en 48 heures.",
                now.plusDays(20), now.plusDays(22), now.plusDays(12), HackathonMode.ONSITE, 3,
                HackathonStatus.REGISTRATION_OPEN, club, organizer, "Casablanca", "Campus universitaire", "IA", "Data");
        addPrize(ai, "1er prix", 10000.0);
        addPrize(ai, "2e prix", 5000.0);
        ai.getSponsors().add(sponsor);

        hack("WebDev Marathon", "Un marathon de développement web en ligne : de l'idée au déploiement.",
                now.plusDays(30), now.plusDays(31), now.plusDays(25), HackathonMode.ONLINE, 100,
                HackathonStatus.REGISTRATION_OPEN, school, organizer, null, null, "Web", "Design");

        hack("Cyber Défense CTF", "Capture the flag : épreuves de cybersécurité pour tous les niveaux.",
                now.plusDays(9), now.plusDays(10), now.plusDays(3), HackathonMode.HYBRID, 50,
                HackathonStatus.REGISTRATION_OPEN, company, organizer, "Rabat", "Technopolis", "Cybersécurité");

        hack("Green IoT Hack", "Objets connectés pour l'environnement : capteurs, énergie, agriculture.",
                now.plusDays(45), now.plusDays(46), now.plusDays(40), HackathonMode.ONSITE, null,
                HackathonStatus.PROPOSED, club, organizer, "Tanger", "Centre d'innovation", "IoT", "Cloud");

        hack("Mobile App Sprint", "Construisez une application mobile en 36 heures.",
                now.minusDays(1), now.plusDays(1), now.minusDays(5), HackathonMode.ONSITE, 40,
                HackathonStatus.ONGOING, school, organizer, "Casablanca", "Salle des projets", "Mobile");

        hack("Data Science Cup", "Compétition de data science sur des jeux de données ouverts.",
                now.minusDays(40), now.minusDays(38), now.minusDays(45), HackathonMode.ONSITE, 60,
                HackathonStatus.FINISHED, company, organizer, "Marrakech", "Palais des congrès", "Data", "IA");

        log.info("DemoDataLoader : 6 hackathons de démo créés.");
    }

    private Organization org(String name, OrganizationType type) {
        Organization o = new Organization();
        o.setName(name);
        o.setType(type);
        return organizationRepository.save(o);
    }

    private void addPrize(Hackathon h, String title, double amount) {
        Prize p = new Prize();
        p.setHackathon(h);
        p.setTitle(title);
        p.setAmount(amount);
        h.getPrizes().add(p);
    }

    private Hackathon hack(String title, String description, LocalDateTime start, LocalDateTime end,
                           LocalDateTime deadline, HackathonMode mode, Integer max, HackathonStatus status,
                           Organization org, User creator, String city, String address, String... themeNames) {
        Hackathon h = new Hackathon();
        h.setTitle(title);
        h.setDescription(description);
        h.setStartDate(start);
        h.setEndDate(end);
        h.setRegistrationDeadline(deadline);
        h.setMode(mode);
        h.setMaxParticipants(max);
        h.setStatus(status);
        h.setOrganization(org);
        h.setCreatedBy(creator);
        h.setExternalLink("https://example.com/" + title.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        Set<String> wanted = Set.copyOf(java.util.Arrays.asList(themeNames));
        h.setThemes(themeRepository.findAll().stream()
                .filter(t -> wanted.contains(t.getName())).collect(Collectors.toSet()));
        if (city != null) {
            h.setLocation(locationRepository.save(new Location(city, address)));
        }
        return hackathonRepository.save(h);
    }
}
