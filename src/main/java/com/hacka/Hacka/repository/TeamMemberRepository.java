package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByTeam_Hackathon_IdAndUser_Id(Long hackathonId, Long userId);
    Optional<TeamMember> findByTeam_Hackathon_IdAndUser_Id(Long hackathonId, Long userId);
}