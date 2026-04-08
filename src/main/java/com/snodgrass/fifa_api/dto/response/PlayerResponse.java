package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.TeamPlayer;

public record PlayerResponse (
    String name,
    Integer number,
    String position,
    Boolean isCaptain
) {
    public static PlayerResponse from(TeamPlayer player) {
        return new PlayerResponse(
                player.getName(),
                player.getNumber(),
                player.getPosition(),
                player.getIsCaptain()
        );
    }
}
