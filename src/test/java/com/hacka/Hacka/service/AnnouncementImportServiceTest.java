package com.hacka.Hacka.service;

import com.hacka.Hacka.dto.ParsedAnnouncement;
import com.hacka.Hacka.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Tests unitaires de la lecture de la réponse de l'IA (sans appel réseau). */
class AnnouncementImportServiceTest {

    private final AnnouncementImportService service = new AnnouncementImportService(mock(ThemeService.class));

    @Test
    void parsesCleanJson() throws Exception {
        ParsedAnnouncement p = service.toParsed("""
                {"title":"AI Challenge","description":"48h d'IA","startDate":"2026-12-05T09:00",
                 "endDate":"2026-12-07T18:00","registrationDeadline":null,"mode":"onsite","city":"Casablanca",
                 "address":null,"externalLink":"https://example.com/register","maxParticipants":100,
                 "themes":["IA","Data"]}""");
        assertEquals("AI Challenge", p.getTitle());
        assertEquals(LocalDateTime.of(2026, 12, 5, 9, 0), p.getStartDate());
        assertNull(p.getRegistrationDeadline());
        assertEquals("ONSITE", p.getMode());
        assertEquals(100, p.getMaxParticipants());
        assertEquals(2, p.getThemes().size());
    }

    @Test
    void stripsMarkdownFences() throws Exception {
        ParsedAnnouncement p = service.toParsed("```json\n{\"title\":\"Hack\"}\n```");
        assertEquals("Hack", p.getTitle());
    }

    @Test
    void ignoresTextAroundJson() throws Exception {
        ParsedAnnouncement p = service.toParsed("Voici le résultat :\n{\"title\":\"Hack\"}\nBonne chance !");
        assertEquals("Hack", p.getTitle());
    }

    @Test
    void badValuesBecomeNull() throws Exception {
        ParsedAnnouncement p = service.toParsed("""
                {"title":"Hack","startDate":"demain","mode":"MOON","externalLink":"javascript:alert(1)",
                 "maxParticipants":-5}""");
        assertNull(p.getStartDate());
        assertNull(p.getMode());
        assertNull(p.getExternalLink());
        assertNull(p.getMaxParticipants());
    }

    @Test
    void missingTitleIsRejected() {
        assertThrows(IllegalStateException.class, () -> service.toParsed("{\"city\":\"Rabat\"}"));
    }

    @Test
    void withoutApiKeyFallsBackToManualEntry() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.parse("annonce de test"));
        assertTrue(ex.getMessage().contains("manuellement"));
    }
}
