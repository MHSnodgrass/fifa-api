package com.snodgrass.fifa_api.dto;

import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;

public record TeamResponse(
        Long id,
        String countryName,
        String countryCode,
        Group groupLetter,
        String flagUrl,
        String logoUrl,
        Integer fifaRanking,
        String managerName,
        TeamStatsResponse stats
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getCountryName(),
                team.getCountryCode(),
                team.getGroupLetter(),
                team.getFlagUrl(),
                team.getLogoUrl(),
                team.getFifaRanking(),
                team.getManagerName(),
                TeamStatsResponse.from(team.getStats())
        );
    }
}
