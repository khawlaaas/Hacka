package com.hacka.Hacka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProfileDto {
    @NotBlank private String firstName;
    @NotBlank
    private String lastName;
    private String city;
    @Size(max = 1000, message = "1000 caractères maximum") private String bio;
    private List<Long> skillIds = new ArrayList<>();
}
