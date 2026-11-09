package com.hacka.Hacka.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Détection de doublons : similarité de Jaccard sur les mots des titres. */
@Component
public class DuplicateDetector {

    private static final double THRESHOLD = 0.6;

    public boolean isSimilar(String titleA, String titleB) {
        return jaccard(tokens(titleA), tokens(titleB)) >= THRESHOLD;
    }

    static Set<String> tokens(String title) {
        if (title == null) return Set.of();
        String s = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return Arrays.stream(s.split("[^a-z0-9]+"))
                .filter(w -> w.length() >= 3)
                .collect(Collectors.toSet());
    }

    static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<String> inter = new HashSet<>(a);
        inter.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) inter.size() / union.size();
    }
}
