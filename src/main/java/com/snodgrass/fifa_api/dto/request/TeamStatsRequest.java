package com.snodgrass.fifa_api.dto.request;

import com.snodgrass.fifa_api.model.TeamStats;
import jakarta.validation.constraints.Min;

public record TeamStatsRequest(
        @Min(0) int matchesPlayed,
        @Min(0) int wins,
        @Min(0) int draws,
        @Min(0) int losses,
        @Min(0) int goalsFor,
        @Min(0) int goalsAgainst,
        int goalDifference,
        @Min(0) int groupPoints,
        @Min(0) int yellowCards,
        @Min(0) int redCards,
        boolean eliminated
) {
    public static TeamStatsRequest from(TeamStats teamStats) {
        return new TeamStatsRequest(
                teamStats.getMatchesPlayed(),
                teamStats.getWins(),
                teamStats.getDraws(),
                teamStats.getLosses(),
                teamStats.getGoalsFor(),
                teamStats.getGoalsAgainst(),
                teamStats.getGoalDifference(),
                teamStats.getGroupPoints(),
                teamStats.getYellowCards(),
                teamStats.getRedCards(),
                teamStats.isEliminated()
        );
    }
}
