package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByHackathonIdOrderByCreatedAtDesc(Long hackathonId);
}
