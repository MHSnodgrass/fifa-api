package com.snodgrass.fifa_api.dto.request;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventRequest(
        @NotNull
        Integer matchNumber,

        @NotNull
        Stage stage,

        Group groupLetter,

        Long homeTeamId,

        Long awayTeamId,

        String homeTeamPlaceholder,

        String awayTeamPlaceholder,

        @NotNull
        LocalDate matchDate,

        LocalTime kickoffTime,

        LocalDateTime kickoffUtc,

        @NotBlank @Size(max = 100)
        String arenaName,

        @NotBlank @Size(max = 100)
        String city,

        @NotNull
        MatchStatus status,

        Integer homeScore,

        Integer awayScore,

        Long winnerTeamId,

        Boolean isDraw,

        boolean hasExtraTime,

        boolean hasPenalties
) {
        public Event toEntity() {
                Event event = new Event();
                event.setMatchNumber(this.matchNumber);
                event.setStage(this.stage);
                event.setGroupLetter(this.groupLetter);
                event.setHomeTeamPlaceholder(this.homeTeamPlaceholder);
                event.setAwayTeamPlaceholder(this.awayTeamPlaceholder);
                event.setMatchDate(this.matchDate);
                event.setKickoffTime(this.kickoffTime);
                event.setKickoffUtc(this.kickoffUtc);
                event.setArenaName(this.arenaName);
                event.setCity(this.city);
                event.setStatus(this.status);
                event.setHomeScore(this.homeScore);
                event.setAwayScore(this.awayScore);
                event.setIsDraw(this.isDraw);
                event.setHasExtraTime(this.hasExtraTime);
                event.setHasPenalties(this.hasPenalties);
                event.setCreatedAt(LocalDateTime.now());
                event.setUpdatedAt(LocalDateTime.now());

                return event;
        }
}
