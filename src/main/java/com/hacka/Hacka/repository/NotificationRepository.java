package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Notification;
import com.hacka.Hacka.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndReadFalse(User user);
    boolean existsByUserAndMessage(User user, String message);
}