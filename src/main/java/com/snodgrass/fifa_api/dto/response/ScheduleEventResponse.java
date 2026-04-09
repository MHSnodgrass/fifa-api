package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.MatchStatus;

public record ScheduleEventResponse(
        Long eventId,
        String arenaName,
        MatchStatus status,
        Integer homeScore,
        Integer awayScore,
        String awayTeamName,
        String awayTeamFlagUrl,
        String homeTeamName,
        String homeTeamFlagUrl
) {
    public static ScheduleEventResponse from(Event event) {
        Team homeTeam = event.getHomeTeam();
        Team awayTeam = event.getAwayTeam();

        String homeName = homeTeam != null ? homeTeam.getCountryName() : event.getHomeTeamPlaceholder();
        String awayName = awayTeam != null ? awayTeam.getCountryName() : event.getAwayTeamPlaceholder();
        String homeFlag = homeTeam != null ? homeTeam.getFlagUrl() : null;
        String awayFlag = awayTeam != null ? awayTeam.getFlagUrl() : null;

        return new ScheduleEventResponse(
                event.getId(),
                event.getArenaName(),
                event.getStatus(),
                event.getHomeScore(),
                event.getAwayScore(),
                awayName,
                awayFlag,
                homeName,
                homeFlag
        );
    }
}
