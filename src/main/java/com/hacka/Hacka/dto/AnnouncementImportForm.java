package com.hacka.Hacka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Formulaire « Importer une annonce » : texte collé par l'organisateur. */
@Getter @Setter
public class AnnouncementImportForm {

    @NotBlank(message = "Collez le texte de l'annonce")
    @Size(min = 20, max = 5000, message = "L'annonce doit faire entre 20 et 5000 caractères")
    private String text;
}
