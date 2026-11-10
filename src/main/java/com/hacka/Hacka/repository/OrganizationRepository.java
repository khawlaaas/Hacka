package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findAllByOrderByNameAsc();
}
