package com.hacka.Hacka.dto;

import com.hacka.Hacka.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Au moins 8 caractères")
    private String password;

    @NotBlank(message = "Veuillez confirmer le mot de passe")
    private String confirmPassword;

    private String city;

    private Role role = Role.PARTICIPANT;   // PARTICIPANT ou ORGANIZER seulement
}
