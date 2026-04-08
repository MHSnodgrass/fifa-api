package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventResponse(
        Long id,
        Integer matchNumber,
        Stage stage,
        Group groupLetter,
        TeamResponse homeTeam,
        TeamResponse awayTeam,
        String homeTeamPlaceholder,
        String awayTeamPlaceholder,
        LocalDate matchDate,
        LocalTime kickoffTime,
        LocalDateTime kickoffUtc,
        String arenaName,
        String city,
        MatchStatus status,
        Integer homeScore,
        Integer awayScore,
        TeamResponse winnerTeam,
        Boolean isDraw,
        boolean hasExtraTime,
        boolean hasPenalties
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getMatchNumber(),
                event.getStage(),
                event.getGroupLetter(),
                event.getHomeTeam() != null ? TeamResponse.from(event.getHomeTeam()) : null,
                event.getAwayTeam() != null ? TeamResponse.from(event.getAwayTeam()) : null,
                event.getHomeTeamPlaceholder(),
                event.getAwayTeamPlaceholder(),
                event.getMatchDate(),
                event.getKickoffTime(),
                event.getKickoffUtc(),
                event.getArenaName(),
                event.getCity(),
                event.getStatus(),
                event.getHomeScore(),
                event.getAwayScore(),
                event.getWinnerTeam() != null ? TeamResponse.from(event.getWinnerTeam()) : null,
                event.getIsDraw(),
                event.isHasExtraTime(),
                event.isHasPenalties()
        );
    }
}
