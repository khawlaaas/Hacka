package com.hacka.Hacka.entity;

public enum HackathonMode {
    ONLINE("En ligne"), ONSITE("Présentiel"), HYBRID("Hybride");

    private final String label;

    HackathonMode(String label) { this.label = label; }

    public String getLabel() { return label; }
}
