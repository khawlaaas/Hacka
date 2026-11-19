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
    private final BadgeService badgeService;

    @Transactional
    public Review add(Long userId, Long hackathonId, ReviewForm form) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
        if (hackathon.getStatus() != HackathonStatus.FINISHED) {
            throw new BusinessException("Vous ne pouvez noter qu'un hackathon terminé.");
        }
        boolean participated = registrationRepository.findByUserIdAndHackathonId(userId, hackathonId)
                .map(r -> r.getStatus() == RegistrationStatus.CONFIRMED)
                .orElse(false);
        if (!participated) {
            throw new BusinessException("Seuls les participants inscrits peuvent laisser un avis.");
        }
        if (reviewRepository.existsByUserIdAndHackathonId(userId, hackathonId)) {
            throw new BusinessException("Vous avez déjà laissé un avis pour ce hackathon.");
        }

        Review review = new Review();
        review.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable.")));
        review.setHackathon(hackathon);
        review.setRating(form.getRating());
        review.setComment(form.getComment());
        Review saved = reviewRepository.save(review);
        badgeService.evaluate(userId);
        return saved;
    }

    public List<Review> reviewsOf(Long hackathonId) {
        return reviewRepository.findByHackathonIdOrderByCreatedAtDesc(hackathonId);
    }

    /** Note moyenne, ou null s'il n'y a aucun avis. */
    public Double average(Long hackathonId) {
        return reviewRepository.averageRating(hackathonId);
    }
}