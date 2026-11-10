package com.hacka.Hacka.service;

import com.hacka.Hacka.dto.SponsorForm;
import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.entity.Sponsor;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.SponsorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SponsorService {
    private final SponsorRepository sponsorRepository;
    private final HackathonService hackathonService;

    @Transactional(readOnly = true)
    public List<Sponsor> findAll() { return sponsorRepository.findAllByOrderByNameAsc(); }

    @Transactional
    public void attach(Long hackathonId, Long sponsorId, User user) {
        Hackathon h = hackathonService.getOwned(hackathonId, user);
        Sponsor s = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new BusinessException("Sponsor introuvable."));
        h.getSponsors().add(s);
    }

    @Transactional
    public void createAndAttach(Long hackathonId, SponsorForm f, User user) {
        Hackathon h = hackathonService.getOwned(hackathonId, user);
        Sponsor s = new Sponsor();
        s.setName(f.getName().trim());
        s.setWebsite(f.getWebsite() == null || f.getWebsite().isBlank() ? null : f.getWebsite().trim());
        s.setLogoUrl(f.getLogoUrl() == null || f.getLogoUrl().isBlank() ? null : f.getLogoUrl().trim());
        h.getSponsors().add(sponsorRepository.save(s));
    }

    @Transactional
    public void detach(Long hackathonId, Long sponsorId, User user) {
        Hackathon h = hackathonService.getOwned(hackathonId, user);
        h.getSponsors().removeIf(s -> s.getId().equals(sponsorId));
    }
}
