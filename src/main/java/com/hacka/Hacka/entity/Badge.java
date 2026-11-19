package com.hacka.Hacka.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "badges")
@Getter @Setter @NoArgsConstructor
public class Badge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code technique unique, ex : FIRST_HACKATHON. */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 10)
    private String icon;

    public Badge(String code, String name, String description, String icon) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }
}

