package com.hacka.Hacka.entity;

/** Cycle de vie d'un hackathon (Livrable 2, figure 1). */
public enum HackathonStatus {
    PROPOSED("Proposé", "secondary"),
    VALIDATED("Validé", "info"),
    REGISTRATION_OPEN("Inscriptions ouvertes", "success"),
    ONGOING("En cours", "primary"),
    FINISHED("Terminé", "dark"),
    REJECTED("Refusé", "danger"),
    CANCELLED("Annulé", "danger");

    private final String label;
    private final String badge;

    HackathonStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() { return label; }
    public String getBadge() { return badge; }

    /** Statuts visibles dans le catalogue public. */
    public boolean isPublic() {
        return this == VALIDATED || this == REGISTRATION_OPEN || this == ONGOING || this == FINISHED;
    }

    /** Le formulaire de modification est accessible. */
    public boolean isEditable() {
        return this == PROPOSED || this == VALIDATED || this == REGISTRATION_OPEN;
    }

    /** Le hackathon peut encore être annulé (tout état non terminal). */
    public boolean isCancellable() {
        return canTransitionTo(CANCELLED);
    }

    /** Suppression définitive possible : uniquement un hackathon jamais publié. */
    public boolean isDeletable() {
        return this == PROPOSED || this == REJECTED;
    }

    /** Machine à états : transitions autorisées. L'annulation est possible depuis tout état non terminal. */
    public boolean canTransitionTo(HackathonStatus next) {
        return switch (this) {
            case PROPOSED -> next == VALIDATED || next == REJECTED || next == CANCELLED;
            case VALIDATED -> next == REGISTRATION_OPEN || next == CANCELLED;
            case REGISTRATION_OPEN -> next == ONGOING || next == CANCELLED;
            case ONGOING -> next == FINISHED || next == CANCELLED;
            case FINISHED, REJECTED, CANCELLED -> false;
        };
    }
}
