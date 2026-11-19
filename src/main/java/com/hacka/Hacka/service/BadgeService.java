package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Badge;
import com.hacka.Hacka.entity.RegistrationStatus;
import com.hacka.Hacka.entity.User;
import com.hacka.Hacka.entity.UserBadge;
import com.hacka.Hacka.repository.BadgeRepository;
import com.hacka.Hacka.repository.FavoriteRepository;
import com.hacka.Hacka.repository.RegistrationRepository;
import com.hacka.Hacka.repository.ReviewRepository;
import com.hacka.Hacka.repository.TeamMemberRepository;
import com.hacka.Hacka.repository.UserBadgeRepository;
import com.hacka.Hacka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;

    /** À appeler après une action (inscription, équipe, avis, favori). */
    @Transactional
    public void evaluate(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        long registrations = registrationRepository.countByUserIdAndStatus(userId, RegistrationStatus.CONFIRMED);
        if (registrations >= 1) {
            award(user, "FIRST_HACKATHON", "Premier hackathon", "Première inscription à un hackathon", "★");
        }
        if (registrations >= 5) {
            award(user, "HACKATHON_ADDICT", "Hackathonien", "Inscrit à au moins 5 hackathons", "♛");
        }
        if (teamMemberRepository.countByUser_Id(userId) >= 1) {
            award(user, "TEAM_PLAYER", "Esprit d'équipe", "Membre d'une équipe", "♣");
        }
        if (reviewRepository.countByUserId(userId) >= 1) {
            award(user, "CRITIC", "Critique", "Premier avis publié", "✎");
        }
        if (favoriteRepository.countByUserId(userId) >= 3) {
            award(user, "COLLECTOR", "Collectionneur", "3 hackathons ajoutés aux favoris", "♥");
        }
    }

    public List<UserBadge> badgesOf(Long userId) {
        return userBadgeRepository.findByUserIdOrderByAwardedAtDesc(userId);
    }

    private void award(User user, String code, String name, String description, String icon) {
        Badge badge = badgeRepository.findByCode(code)
                .orElseGet(() -> badgeRepository.save(new Badge(code, name, description, icon)));
        if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
            userBadgeRepository.save(new UserBadge(user, badge));
        }
    }
}
