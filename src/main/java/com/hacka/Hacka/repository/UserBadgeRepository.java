package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    List<UserBadge> findByUserIdOrderByAwardedAtDesc(Long userId);
}
