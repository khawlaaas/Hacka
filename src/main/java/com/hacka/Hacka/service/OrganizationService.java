package com.hacka.Hacka.service;

import com.hacka.Hacka.dto.OrganizationForm;
import com.hacka.Hacka.entity.Organization;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.HackathonRepository;
import com.hacka.Hacka.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final HackathonRepository hackathonRepository;

    @Transactional(readOnly = true)
    public List<Organization> findAll() { return organizationRepository.findAllByOrderByNameAsc(); }

    @Transactional(readOnly = true)
    public Organization find(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Organisation introuvable."));
    }

    @Transactional
    public Organization create(OrganizationForm f) {
        Organization o = new Organization();
        apply(f, o);
        return organizationRepository.save(o);
    }

    @Transactional
    public Organization update(Long id, OrganizationForm f) {
        Organization o = find(id);
        apply(f, o);
        return o;
    }

    @Transactional
    public void delete(Long id) {
        Organization o = find(id);
        if (hackathonRepository.existsByOrganizationId(id)) {
            throw new BusinessException("Cette organisation a des hackathons : elle ne peut pas être supprimée.");
        }
        organizationRepository.delete(o);
    }

    private void apply(OrganizationForm f, Organization o) {
        o.setName(f.getName().trim());
        o.setType(f.getType());
        o.setWebsite(blankToNull(f.getWebsite()));
        o.setLogoUrl(blankToNull(f.getLogoUrl()));
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
