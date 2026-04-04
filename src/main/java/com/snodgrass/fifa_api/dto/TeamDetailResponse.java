package com.snodgrass.fifa_api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
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
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static TeamDetailResponse from(Team team) {
        List<PlayerResponse> parsedSquad = List.of();

        if (team.getSquad() != null && !team.getSquad().isBlank()) {
            try {
                parsedSquad = MAPPER.readValue(team.getSquad(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to parse squad JSON for team ID {}", team.getId(), e);
            }
        }

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
