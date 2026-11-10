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

@Service@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggle(Long userId, Long hid) {          // true = ajouté, false = retiré
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndHackathonId(userId, hid);
        if (existing.isPresent()) { favoriteRepository.delete(existing.get()); return false; }
        Favorite f = new Favorite();
        f.setUser(userRepository.findById(userId).orElseThrow());
        f.setHackathon(hackathonRepository.findById(hid)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable.")));
        favoriteRepository.save(f);
        return true;
    }
    public boolean isFavorite(Long userId, Long hid) {
        return favoriteRepository.findByUserIdAndHackathonId(userId, hid).isPresent();
    }
    public List<Favorite> list(Long userId) {
        return favoriteRepository.findByUserIdOrderByAddedAtDesc(userId);
    }
}