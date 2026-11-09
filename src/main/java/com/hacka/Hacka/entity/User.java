package com.hacka.Hacka.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")          // "user" est un mot réservé dans certaines bases
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;            // toujours encodé BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.PARTICIPANT;

    private String city;

    @Column(length = 1000)
    private String bio;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean enabled = true;     // champ en plus : l'admin peut bloquer un compte

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }
}