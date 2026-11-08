package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndHackathonId(Long userId, Long hackathonId);
    List<Favorite> findByUserIdOrderByAddedAtDesc(Long userId);
}
