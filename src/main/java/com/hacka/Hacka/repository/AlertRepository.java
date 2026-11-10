package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Alert;
import com.hacka.Hacka.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserOrderByCreatedAtDesc(User user);
    List<Alert> findByActiveTrue();
}
