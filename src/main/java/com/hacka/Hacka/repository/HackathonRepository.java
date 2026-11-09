package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.entity.HackathonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface HackathonRepository extends JpaRepository<Hackathon, Long>, JpaSpecificationExecutor<Hackathon> {

    List<Hackathon> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    /** File d'attente de validation de l'admin (les plus anciens d'abord). */
    List<Hackathon> findByStatusOrderByCreatedAtAsc(HackathonStatus status);

    List<Hackathon> findByStatusIn(Collection<HackathonStatus> statuses);

    List<Hackathon> findTop6ByStatusOrderByStartDateAsc(HackathonStatus status);

    List<Hackathon> findByStatusInAndEndDateGreaterThanEqualOrderByStartDateAsc(
            Collection<HackathonStatus> statuses, LocalDateTime from);

    long countByStatus(HackathonStatus status);

    boolean existsByOrganizationId(Long organizationId);

    boolean existsByThemesId(Long themeId);

    @Query("""
            select h from Hackathon h
            where h.status = :status
              and h.registrationDeadline between :from and :to
            order by h.registrationDeadline asc
            """)
    List<Hackathon> findClosingSoon(@Param("status") HackathonStatus status,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    /** Candidats pour la détection de doublons : dates proches, hors refusés/annulés. */
    @Query("""
            select h from Hackathon h
            where h.status not in :excluded
              and h.startDate between :from and :to
            """)
    List<Hackathon> findNearStart(@Param("excluded") Collection<HackathonStatus> excluded,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);
}
