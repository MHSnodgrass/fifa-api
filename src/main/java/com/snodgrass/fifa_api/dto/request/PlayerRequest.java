package com.snodgrass.fifa_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record PlayerRequest(
        @NotBlank
        String name,

        @NotNull @Min(1)
        Integer number,

        @NotBlank
        String position,

        Boolean isCaptain
) {}
