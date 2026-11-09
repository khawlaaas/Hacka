package com.hacka.Hacka.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "team_requests")
@Getter @Setter @NoArgsConstructor
public class TeamRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")           // le joueur invité OU candidat
    private User user;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RequestType type;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(length = 500)
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
}
