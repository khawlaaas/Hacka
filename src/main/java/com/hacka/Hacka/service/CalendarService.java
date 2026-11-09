package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Hackathon;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Calendrier : regroupement par mois et export .ics (iCalendar). */
@Service
public class CalendarService {

    private static final DateTimeFormatter ICS_LOCAL = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter ICS_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public Map<String, List<Hackathon>> groupByMonth(List<Hackathon> hackathons) {
        Map<String, List<Hackathon>> groups = new LinkedHashMap<>();
        for (Hackathon h : hackathons) {
            String key = h.getStartDate().getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    + " " + h.getStartDate().getYear();
            groups.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(h);
        }
        return groups;
    }

    /** Heures « flottantes » : l'agenda de l'utilisateur les affiche à l'heure locale saisie. */
    public byte[] toIcs(List<Hackathon> hackathons, String baseUrl) {
        StringBuilder sb = new StringBuilder();
        line(sb, "BEGIN:VCALENDAR");
        line(sb, "VERSION:2.0");
        line(sb, "PRODID:-//Hacka//Hackathons//FR");
        line(sb, "CALSCALE:GREGORIAN");
        line(sb, "X-WR-CALNAME:Hacka - Hackathons");
        String stamp = LocalDateTime.now(ZoneOffset.UTC).format(ICS_UTC);
        for (Hackathon h : hackathons) {
            line(sb, "BEGIN:VEVENT");
            line(sb, "UID:hackathon-" + h.getId() + "@hacka");
            line(sb, "DTSTAMP:" + stamp);
            line(sb, "DTSTART:" + h.getStartDate().format(ICS_LOCAL));
            line(sb, "DTEND:" + h.getEndDate().format(ICS_LOCAL));
            line(sb, "SUMMARY:" + escape(h.getTitle()));
            if (h.getDescription() != null && !h.getDescription().isBlank()) {
                line(sb, "DESCRIPTION:" + escape(h.getDescription()));
            }
            if (h.getLocation() != null) {
                String loc = h.getLocation().getCity();
                if (h.getLocation().getAddress() != null && !h.getLocation().getAddress().isBlank()) {
                    loc = h.getLocation().getAddress() + ", " + loc;
                }
                line(sb, "LOCATION:" + escape(loc));
            }
            line(sb, "URL:" + baseUrl + "/hackathons/" + h.getId());
            line(sb, "END:VEVENT");
        }
        line(sb, "END:VCALENDAR");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,")
                .replace("\r\n", "\\n").replace("\n", "\\n").replace("\r", "\\n");
    }

    /** Pliage des lignes à ~70 caractères (RFC 5545). */
    private static void line(StringBuilder sb, String s) {
        int i = 0;
        boolean first = true;
        while (i < s.length()) {
            int end = Math.min(s.length(), i + (first ? 70 : 69));
            if (!first) sb.append(' ');
            sb.append(s, i, end).append("\r\n");
            i = end;
            first = false;
        }
    }
}
