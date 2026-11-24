package com.hacka.Hacka.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Résultat brut de l'extraction par l'IA. Tout champ peut être null : l'organisateur complète. */
@Getter @Setter
public class ParsedAnnouncement {
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime registrationDeadline;
    private String mode;               // ONLINE, ONSITE, HYBRID ou null
    private String city;
    private String address;
    private String externalLink;
    private Integer maxParticipants;
    private List<String> themes = new ArrayList<>();
}
