package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;

import java.util.List;

public record TeamDetailResponse(
        Long id,
        String countryName,
        String countryCode,
        Group groupLetter,
        String flagUrl,
        String logoUrl,
        Integer fifaRanking,
        String managerName,
        TeamStatsResponse stats,
        List<PlayerResponse> squad
) {
    public static TeamDetailResponse from(Team team) {
        List<PlayerResponse> parsedSquad = team.getSquad() == null
                ? List.of()
                : team.getSquad().stream().map(PlayerResponse::from).toList();

        return new TeamDetailResponse(
                team.getId(),
                team.getCountryName(),
                team.getCountryCode(),
                team.getGroupLetter(),
                team.getFlagUrl(),
                team.getLogoUrl(),
                team.getFifaRanking(),
                team.getManagerName(),
                TeamStatsResponse.from(team.getStats()),
                parsedSquad
        );
    }
}
