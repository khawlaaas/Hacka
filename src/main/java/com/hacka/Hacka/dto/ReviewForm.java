package com.hacka.Hacka.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewForm {
    @Min(value = 1, message = "Note entre 1 et 5") @Max(value = 5, message = "Note entre 1 et 5")
    private int rating = 5;
    @Size(max = 1000, message = "1000 caractères maximum") private String comment;
}
