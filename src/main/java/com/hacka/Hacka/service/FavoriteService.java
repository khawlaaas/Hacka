package com.hacka.Hacka.service;

import com.hacka.Hacka.repository.*;

import com.hacka.Hacka.entity.*;

import com.hacka.Hacka.entity.Favorite;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final BadgeService badgeService;

    /** Ajoute ou retire le favori. Retourne true si le favori vient d'être ajouté. */
    @Transactional
    public boolean toggle(Long userId, Long hackathonId) {
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndHackathonId(userId, hackathonId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }
        Favorite favorite = new Favorite();
        favorite.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable.")));
        favorite.setHackathon(hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable.")));
        favoriteRepository.save(favorite);
        badgeService.evaluate(userId);
        return true;
    }

    public boolean isFavorite(Long userId, Long hackathonId) {
        return favoriteRepository.findByUserIdAndHackathonId(userId, hackathonId).isPresent();
    }

    public List<Favorite> list(Long userId) {
        return favoriteRepository.findByUserIdOrderByAddedAtDesc(userId);
    }
}
