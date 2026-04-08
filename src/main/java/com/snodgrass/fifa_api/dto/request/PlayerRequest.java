package com.snodgrass.fifa_api.dto.request;

import com.snodgrass.fifa_api.model.TeamPlayer;
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
) {
        public static PlayerRequest from(TeamPlayer player) {
              return new PlayerRequest(
                      player.getName(),
                      player.getNumber(),
                      player.getPosition(),
                      player.getIsCaptain()
              );
        }
}
