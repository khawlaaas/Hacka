package com.hacka.Hacka.config;

import com.hacka.Hacka.entity.Theme;
import com.hacka.Hacka.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Thèmes de référence. Vocabulaire commun avec les compétences (Skill) : le moteur de
 * recommandation compare les noms texte par texte, donc A doit utiliser les mêmes noms.
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class ReferenceDataLoader implements CommandLineRunner {
    private final ThemeRepository themeRepository;

    @Override
    public void run(String... args) {
        for (String name : List.of("IA", "Web", "Cybersécurité", "Mobile", "Data", "Design", "Cloud", "IoT")) {
            if (!themeRepository.existsByNameIgnoreCase(name)) {
                themeRepository.save(new Theme(name));
            }
        }
    }
}
