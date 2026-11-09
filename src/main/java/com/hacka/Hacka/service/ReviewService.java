package com.hacka.Hacka.service;

import com.hacka.Hacka.repository.*;

import com.hacka.Hacka.entity.*;

import com.hacka.Hacka.dto.ReviewForm;
import com.hacka.Hacka.entity.RegistrationStatus;
import com.hacka.Hacka.entity.Review;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.RegistrationRepository;
import com.hacka.Hacka.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final RegistrationRepository registrationRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;

    @Transactional
    public Review add(Long userId, Long hid, ReviewForm form) {
        Hackathon h = hackathonRepository.findById(hid)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
        if (h.getStatus() != HackathonStatus.FINISHED)
            throw new BusinessException("Vous ne pouvez noter qu'un hackathon terminé.");
        boolean participated = registrationRepository.findByUserIdAndHackathonId(userId, hid)
                .map(r -> r.getStatus() == RegistrationStatus.CONFIRMED).orElse(false);
        if (!participated)
            throw new BusinessException("Seuls les participants inscrits peuvent laisser un avis.");
        if (reviewRepository.existsByUserIdAndHackathonId(userId, hid))
            throw new BusinessException("Vous avez déjà laissé un avis pour ce hackathon.");

        Review r = new Review();
        r.setUser(userRepository.findById(userId).orElseThrow());
        r.setHackathon(h);
        r.setRating(form.getRating());
        r.setComment(form.getComment());
        return reviewRepository.save(r);
    }
    public List<Review> reviewsOf(Long hid) { return reviewRepository.findByHackathonIdOrderByCreatedAtDesc(hid); }
    public Double average(Long hid) { return reviewRepository.averageRating(hid); }
}