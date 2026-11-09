package com.hacka.Hacka.dto;

import com.hacka.Hacka.entity.Organization;
import com.hacka.Hacka.entity.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrganizationForm {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150, message = "150 caractères maximum")
    private String name;

    @NotNull(message = "Le type est obligatoire")
    private OrganizationType type;

    @Size(max = 255, message = "255 caractères maximum")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "Le site doit commencer par http:// ou https://")
    private String website;

    @Size(max = 255, message = "255 caractères maximum")
    private String logoUrl;

    public static OrganizationForm from(Organization o) {
        OrganizationForm f = new OrganizationForm();
        f.name = o.getName();
        f.type = o.getType();
        f.website = o.getWebsite();
        f.logoUrl = o.getLogoUrl();
        return f;
    }
}
