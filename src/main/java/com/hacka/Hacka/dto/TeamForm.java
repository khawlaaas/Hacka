package com.hacka.Hacka.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TeamForm {
    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(max = 100, message = "100 caractères maximum")
    private String name;

    @Size(max = 1000) private String description;
    @Size(max = 255) private String lookingForSkills;

    @Min(value = 2, message = "Une équipe compte au moins 2 places")
    @Max(value = 6, message = "Une équipe compte 6 places maximum")
    private int maxSize = 4;
}

