package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Theme;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.HackathonRepository;
import com.hacka.Hacka.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Les thèmes sont gérés par l'admin (module A appelle ces méthodes). */
@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final HackathonRepository hackathonRepository;

    @Transactional(readOnly = true)
    public List<Theme> findAll() { return themeRepository.findAllByOrderByNameAsc(); }

    @Transactional
    public Theme create(String name) {
        if (name == null || name.isBlank()) throw new BusinessException("Le nom du thème est obligatoire.");
        String n = name.trim();
        if (themeRepository.existsByNameIgnoreCase(n)) throw new BusinessException("Ce thème existe déjà.");
        return themeRepository.save(new Theme(n));
    }
    /** Identifiants des thèmes dont le nom correspond (sans tenir compte de la casse) : import IA. */
    @Transactional(readOnly = true)
    public Set<Long> idsByNames(Collection<String> names) {
        Set<Long> ids = new HashSet<>();
        if (names == null) return ids;
        for (Theme t : findAll()) {
            for (String n : names) {
                if (n != null && n.trim().equalsIgnoreCase(t.getName())) ids.add(t.getId());
            }
        }
        return ids;
    }

    @Transactional
    public void delete(Long id) {
        Theme t = themeRepository.findById(id).orElseThrow(() -> new BusinessException("Thème introuvable."));
        if (hackathonRepository.existsByThemesId(id)) {
            throw new BusinessException("Ce thème est utilisé par des hackathons : il ne peut pas être supprimé.");
        }
        themeRepository.delete(t);
    }
}
