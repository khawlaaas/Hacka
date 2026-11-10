package com.hacka.Hacka.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hackathon_id", nullable = false) private Hackathon hackathon;

    @Column(nullable = false, length = 500) private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) private ReportStatus status = ReportStatus.OPEN;

    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
}