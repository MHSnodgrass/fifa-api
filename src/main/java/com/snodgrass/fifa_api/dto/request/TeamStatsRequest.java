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
    public TeamStats toEntity() {
        TeamStats stats = new TeamStats();

        stats.setMatchesPlayed(this.matchesPlayed);
        stats.setWins(this.wins);
        stats.setDraws(this.draws);
        stats.setLosses(this.losses);
        stats.setGoalsFor(this.goalsFor);
        stats.setGoalsAgainst(this.goalsAgainst);
        stats.setGoalDifference(this.goalDifference);
        stats.setGroupPoints(this.groupPoints);
        stats.setYellowCards(this.yellowCards);
        stats.setRedCards(this.redCards);
        stats.setEliminated(this.eliminated);

        return stats;
    }
}
