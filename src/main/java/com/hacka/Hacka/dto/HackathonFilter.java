package com.hacka.Hacka.dto;

import com.hacka.Hacka.entity.HackathonMode;
import com.hacka.Hacka.entity.HackathonStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Paramètres de recherche du catalogue (liés aux paramètres GET). */
@Getter @Setter
public class HackathonFilter {
    private String keyword;
    private String city;
    private HackathonMode mode;
    private Long themeId;
    private HackathonStatus status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;
    private boolean onlyAvailable;
}
