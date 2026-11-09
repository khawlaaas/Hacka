package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    List<Theme> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}
