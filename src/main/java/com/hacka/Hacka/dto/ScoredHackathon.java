package com.hacka.Hacka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScoredHackathon {
    private Hackathon hackathon;
    private double score;
}