package com.hacka.Hacka.dto;

import com.hacka.Hacka.entity.HackathonMode;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertDto {
    @Size(max = 255, message = "255 caractères maximum") private String keywords;
    @Size(max = 100, message = "100 caractères maximum") private String city;
    private HackathonMode mode;     // facultatif
    private Long themeId;           // facultatif
}
