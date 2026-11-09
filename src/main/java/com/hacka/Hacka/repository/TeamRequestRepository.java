package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.RequestStatus;
import com.hacka.Hacka.entity.RequestType;
import com.hacka.Hacka.entity.TeamRequest;
import com.hacka.Hacka.entity.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRequestRepository extends JpaRepository<TeamRequest, Long> {
    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, Long userId, RequestStatus status);

    // invitations reçues par un joueur
    List<TeamRequest> findByUserIdAndTypeAndStatus(Long userId, RequestType type, RequestStatus status);

    // candidatures reçues par les équipes que je dirige
    @Query("""
        select r from TeamRequest r join r.team t join t.members m
        where m.user.id = :uid and m.role = :role and r.type = :type and r.status = :status
        """)
    List<TeamRequest> findForLeader(@Param("uid") Long userId, @Param("role") TeamRole role,
                                    @Param("type") RequestType type, @Param("status") RequestStatus status);
}
