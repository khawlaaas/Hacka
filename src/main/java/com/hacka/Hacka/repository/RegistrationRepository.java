package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Registration;
import com.hacka.Hacka.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUserIdAndHackathonId(Long userId, Long hackathonId);
    long countByHackathonIdAndStatus(Long hackathonId, RegistrationStatus status);
    List<Registration> findByUserIdOrderByRegistrationDateDesc(Long userId);


    @Query("""
        select r.user from Registration r
        where r.hackathon.id = :hid and r.status = :status
          and not exists (select 1 from TeamMember tm
                          where tm.user = r.user and tm.team.hackathon.id = :hid)
        """)
    List<User> findUnteamedParticipants(@Param("hid") Long hackathonId,
                                        @Param("status") RegistrationStatus status);
}