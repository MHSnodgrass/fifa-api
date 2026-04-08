package com.snodgrass.fifa_api.dto.request;

import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TeamRequest(
        @NotBlank @Size(max = 100)
        String countryName,

        @NotBlank @Size(min = 3, max = 3)
        String countryCode,

        @NotNull
        Group groupLetter,

        String flagUrl,

        String logoUrl,

        @Min(1)
        Integer fifaRanking,

        @Size(max = 100)
        String managerName,

        @Valid
        List<PlayerRequest> squad,

        @Valid
        TeamStatsRequest stats
) {
        public static TeamRequest from(Team team) {
              return new TeamRequest(
                      team.getCountryName(),
                      team.getCountryCode(),
                      team.getGroupLetter(),
                      team.getFlagUrl(),
                      team.getLogoUrl(),
                      team.getFifaRanking(),
                      team.getManagerName(),
                      team.getSquad() == null ? List.of() : team.getSquad().stream().map(PlayerRequest::from).toList(),
                      TeamStatsRequest.from(team.getStats())
              );
        }
}
