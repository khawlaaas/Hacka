package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
    List<Sponsor> findAllByOrderByNameAsc();
}
