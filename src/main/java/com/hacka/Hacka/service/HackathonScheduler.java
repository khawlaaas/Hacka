package com.hacka.Hacka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Fait évoluer automatiquement les statuts des hackathons selon leurs dates (BF-27). */
@Slf4j
@Component
@RequiredArgsConstructor
public class HackathonScheduler {
    private final HackathonService hackathonService;

    @Scheduled(initialDelay = 10_000, fixedDelay = 60_000)
    public void refreshStatuses() {
        int changed = hackathonService.refreshStatuses();
        if (changed > 0) log.info("Statuts de hackathons mis à jour : {}", changed);
    }
}
