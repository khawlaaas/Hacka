package com.hacka.Hacka.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_badges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"}))
@Getter @Setter @NoArgsConstructor
public class UserBadge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @Column(nullable = false)
    private LocalDateTime awardedAt = LocalDateTime.now();

    public UserBadge(User user, Badge badge) {
        this.user = user;
        this.badge = badge;
    }
}
