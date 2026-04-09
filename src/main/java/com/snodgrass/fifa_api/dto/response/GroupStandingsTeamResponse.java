package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.Team;

public record GroupStandingsTeamResponse(
        Long teamId,
        String countryName,
        String countryCode,
        String flagUrl,
        Integer fifaRanking,
        TeamStatsResponse stats
) {
    public static GroupStandingsTeamResponse from(Team team) {
        return new GroupStandingsTeamResponse(
                team.getId(),
                team.getCountryName(),
                team.getCountryCode(),
                team.getFlagUrl(),
                team.getFifaRanking(),
                TeamStatsResponse.from(team.getStats())
        );
    }
}
