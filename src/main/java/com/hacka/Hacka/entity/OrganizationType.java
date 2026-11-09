package com.hacka.Hacka.entity;

public enum OrganizationType {
    CLUB("Club"), SCHOOL("École"), COMPANY("Entreprise");

    private final String label;

    OrganizationType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
