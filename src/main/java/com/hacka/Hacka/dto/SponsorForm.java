package com.hacka.Hacka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SponsorForm {
    @NotBlank(message = "Le nom du sponsor est obligatoire")
    @Size(max = 150, message = "150 caractères maximum")
    private String name;

    @Size(max = 255, message = "255 caractères maximum")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "Le site doit commencer par http:// ou https://")
    private String website;

    @Size(max = 255, message = "255 caractères maximum")
    private String logoUrl;
}
