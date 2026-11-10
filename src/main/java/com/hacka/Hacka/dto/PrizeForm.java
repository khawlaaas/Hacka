package com.hacka.Hacka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrizeForm {
    @NotBlank(message = "Le titre du prix est obligatoire")
    @Size(max = 100, message = "100 caractères maximum")
    private String title;

    @Size(max = 255, message = "255 caractères maximum")
    private String description;

    @PositiveOrZero(message = "Le montant ne peut pas être négatif")
    private Double amount;
}
