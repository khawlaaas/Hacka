package com.hacka.Hacka.dto;

import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.entity.HackathonMode;
import com.hacka.Hacka.entity.Theme;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Formulaire de publication / modification d'un hackathon. */
@Getter @Setter
public class HackathonForm {

    private static final String DT = "yyyy-MM-dd'T'HH:mm";

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "200 caractères maximum")
    private String title;

    @Size(max = 5000, message = "5000 caractères maximum")
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @DateTimeFormat(pattern = DT)
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    @DateTimeFormat(pattern = DT)
    private LocalDateTime endDate;

    @DateTimeFormat(pattern = DT)
    private LocalDateTime registrationDeadline;

    @NotNull(message = "Le mode est obligatoire")
    private HackathonMode mode;

    @Min(value = 1, message = "Le nombre de places doit être supérieur à 0")
    private Integer maxParticipants;

    @Size(max = 255, message = "255 caractères maximum")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "Le lien doit commencer par http:// ou https://")
    private String externalLink;

    @NotNull(message = "L'organisation est obligatoire")
    private Long organizationId;

    @NotEmpty(message = "Choisissez au moins un thème")
    private Set<Long> themeIds = new HashSet<>();

    @Size(max = 100, message = "100 caractères maximum")
    private String locationCity;

    @Size(max = 255, message = "255 caractères maximum")
    private String locationAddress;

    /** Coché par l'organisateur pour publier malgré un doublon probable. */
    private boolean ignoreDuplicates;

    @AssertTrue(message = "La date de fin doit être postérieure à la date de début, et la date limite d'inscription antérieure au début")
    public boolean isDatesConsistent() {
        if (startDate == null || endDate == null) return true;
        if (endDate.isBefore(startDate)) return false;
        return registrationDeadline == null || !registrationDeadline.isAfter(startDate);
    }

    @AssertTrue(message = "La ville est obligatoire pour un hackathon en présentiel ou hybride")
    public boolean isLocationConsistent() {
        if (mode == null || mode == HackathonMode.ONLINE) return true;
        return locationCity != null && !locationCity.isBlank();
    }

    public static HackathonForm from(Hackathon h) {
        HackathonForm f = new HackathonForm();
        f.title = h.getTitle();
        f.description = h.getDescription();
        f.startDate = h.getStartDate();
        f.endDate = h.getEndDate();
        f.registrationDeadline = h.getRegistrationDeadline();
        f.mode = h.getMode();
        f.maxParticipants = h.getMaxParticipants();
        f.externalLink = h.getExternalLink();
        f.organizationId = h.getOrganization().getId();
        f.themeIds = h.getThemes().stream().map(Theme::getId).collect(Collectors.toSet());
        if (h.getLocation() != null) {
            f.locationCity = h.getLocation().getCity();
            f.locationAddress = h.getLocation().getAddress();
        }
        return f;
    }
}
