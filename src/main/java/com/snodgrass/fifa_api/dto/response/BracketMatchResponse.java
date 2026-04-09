package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.MatchStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record BracketMatchResponse(
        Long eventId,
        Integer matchNumber,
        LocalDate matchDate,
        LocalTime kickoffTime,
        MatchStatus status,
        String arenaName,
        String city,
        String homeTeamName,
        String homeTeamFlagUrl,
        String awayTeamName,
        String awayTeamFlagUrl,
        Integer homeScore,
        Integer awayScore,
        Long winnerTeamId,
        String winnerTeamName
) {
    public static BracketMatchResponse from(Event event) {
        Team homeTeam = event.getHomeTeam();
        Team awayTeam = event.getAwayTeam();
        Team winner = event.getWinnerTeam();

        return new BracketMatchResponse(
                event.getId(),
                event.getMatchNumber(),
                event.getMatchDate(),
                event.getKickoffTime(),
                event.getStatus(),
                event.getArenaName(),
                event.getCity(),
                homeTeam != null ? homeTeam.getCountryName() : event.getHomeTeamPlaceholder(),
                homeTeam != null ? homeTeam.getFlagUrl() : null,
                awayTeam != null ? awayTeam.getCountryName() : event.getAwayTeamPlaceholder(),
                awayTeam != null ? awayTeam.getFlagUrl() : null,
                event.getHomeScore(),
                event.getAwayScore(),
                winner != null ? winner.getId() : null,
                winner != null ? winner.getCountryName() : null
        );
    }
}
