package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Registration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Liste et export CSV des inscrits d'un hackathon (espace organisateur). */
@Service
public class RegistrantService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<Registration> registrants(Long hackathonId) {
        return em.createQuery("""
                        select r from Registration r join fetch r.user
                        where r.hackathon.id = :hid
                        order by r.registrationDate asc
                        """, Registration.class)
                .setParameter("hid", hackathonId)
                .getResultList();
    }

    /** CSV séparé par des points-virgules (ouverture directe dans Excel), avec BOM UTF-8. */
    public byte[] toCsv(List<Registration> registrations) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Nom;Prénom;Email;Ville;Date d'inscription;Statut\r\n");
        for (Registration r : registrations) {
            sb.append(cell(r.getUser().getLastName())).append(';')
              .append(cell(r.getUser().getFirstName())).append(';')
              .append(cell(r.getUser().getEmail())).append(';')
              .append(cell(r.getUser().getCity())).append(';')
              .append(cell(HackathonService.formatDate(r.getRegistrationDate()))).append(';')
              .append(cell(r.getStatus().name())).append("\r\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String cell(String v) {
        if (v == null) return "";
        String s = v;
        // protection contre l'injection de formules dans Excel
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
