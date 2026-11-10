package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Theme;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.HackathonRepository;
import com.hacka.Hacka.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public void delete(Long id) {
        Theme t = themeRepository.findById(id).orElseThrow(() -> new BusinessException("Thème introuvable."));
        if (hackathonRepository.existsByThemesId(id)) {
            throw new BusinessException("Ce thème est utilisé par des hackathons : il ne peut pas être supprimé.");
        }
        themeRepository.delete(t);
    }
}
