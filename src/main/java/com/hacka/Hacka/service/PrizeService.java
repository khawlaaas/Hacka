package com.hacka.Hacka.service;

import com.hacka.Hacka.dto.PrizeForm;
import com.hacka.Hacka.entity.Hackathon;
import com.hacka.Hacka.entity.Prize;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.PrizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrizeService {
    private final PrizeRepository prizeRepository;
    private final HackathonService hackathonService;

    @Transactional
    public Prize add(Long hackathonId, PrizeForm f, User user) {
        Hackathon h = hackathonService.getOwned(hackathonId, user);
        Prize p = new Prize();
        p.setHackathon(h);
        p.setTitle(f.getTitle().trim());
        p.setDescription(f.getDescription() == null || f.getDescription().isBlank() ? null : f.getDescription().trim());
        p.setAmount(f.getAmount());
        return prizeRepository.save(p);
    }

    @Transactional
    public void remove(Long hackathonId, Long prizeId, User user) {
        hackathonService.getOwned(hackathonId, user);
        Prize p = prizeRepository.findById(prizeId)
                .orElseThrow(() -> new BusinessException("Prix introuvable."));
        if (!p.getHackathon().getId().equals(hackathonId)) {
            throw new BusinessException("Ce prix n'appartient pas à ce hackathon.");
        }
        prizeRepository.delete(p);
    }
}
