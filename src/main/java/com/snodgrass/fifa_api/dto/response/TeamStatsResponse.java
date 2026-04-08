package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.TeamStats;

public record TeamStatsResponse(
        int matchesPlayed,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        int groupPoints,
        int yellowCards,
        int redCards,
        boolean eliminated
) {
    public static TeamStatsResponse from(TeamStats stats) {
        if (stats == null) return null;
        return new TeamStatsResponse(
                stats.getMatchesPlayed(),
                stats.getWins(),
                stats.getDraws(),
                stats.getLosses(),
                stats.getGoalsFor(),
                stats.getGoalsAgainst(),
                stats.getGoalDifference(),
                stats.getGroupPoints(),
                stats.getYellowCards(),
                stats.getRedCards(),
                stats.isEliminated()
        );
    }
}
