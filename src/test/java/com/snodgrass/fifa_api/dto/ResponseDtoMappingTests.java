package com.snodgrass.fifa_api.dto;

import com.snodgrass.fifa_api.dto.response.*;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.TeamPlayer;
import com.snodgrass.fifa_api.model.TeamStats;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDtoMappingTests {

    private Team team;
    private TeamStats stats;

    @BeforeEach
    void setUp() {
        stats = new TeamStats();
        stats.setMatchesPlayed(3);
        stats.setWins(2);
        stats.setDraws(1);
        stats.setLosses(0);
        stats.setGoalsFor(5);
        stats.setGoalsAgainst(2);
        stats.setGoalDifference(3);
        stats.setGroupPoints(7);
        stats.setYellowCards(3);
        stats.setRedCards(1);
        stats.setEliminated(false);

        team = new Team();
        team.setId(1L);
        team.setCountryName("Brazil");
        team.setCountryCode("BRA");
        team.setGroupLetter(Group.A);
        team.setFlagUrl("/flags/bra.png");
        team.setLogoUrl("/logos/bra.png");
        team.setFifaRanking(1);
        team.setManagerName("Dorival Júnior");
        team.setStats(stats);
        team.setSquad(List.of(new TeamPlayer("Neymar Jr", 10, "FW", true)));
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
    }

    // PlayerResponse.from()

    @Test
    void playerResponse_from_mapsAllFields() {
        TeamPlayer player = new TeamPlayer("Neymar Jr", 10, "FW", true);

        PlayerResponse response = PlayerResponse.from(player);

        assertThat(response.name()).isEqualTo("Neymar Jr");
        assertThat(response.number()).isEqualTo(10);
        assertThat(response.position()).isEqualTo("FW");
        assertThat(response.isCaptain()).isTrue();
    }

    @Test
    void playerResponse_from_withNullCaptain() {
        TeamPlayer player = new TeamPlayer("Alisson", 1, "GK", null);

        PlayerResponse response = PlayerResponse.from(player);

        assertThat(response.isCaptain()).isNull();
    }

    // TeamStatsResponse.from()

    @Test
    void teamStatsResponse_from_mapsAllFields() {
        TeamStatsResponse response = TeamStatsResponse.from(stats);

        assertThat(response.matchesPlayed()).isEqualTo(3);
        assertThat(response.wins()).isEqualTo(2);
        assertThat(response.draws()).isEqualTo(1);
        assertThat(response.losses()).isEqualTo(0);
        assertThat(response.goalsFor()).isEqualTo(5);
        assertThat(response.goalsAgainst()).isEqualTo(2);
        assertThat(response.goalDifference()).isEqualTo(3);
        assertThat(response.groupPoints()).isEqualTo(7);
        assertThat(response.yellowCards()).isEqualTo(3);
        assertThat(response.redCards()).isEqualTo(1);
        assertThat(response.eliminated()).isFalse();
    }

    @Test
    void teamStatsResponse_from_withNull_returnsNull() {
        TeamStatsResponse response = TeamStatsResponse.from(null);
        assertThat(response).isNull();
    }

    // TeamResponse.from()

    @Test
    void teamResponse_from_mapsAllFields() {
        TeamResponse response = TeamResponse.from(team);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.countryName()).isEqualTo("Brazil");
        assertThat(response.countryCode()).isEqualTo("BRA");
        assertThat(response.groupLetter()).isEqualTo(Group.A);
        assertThat(response.flagUrl()).isEqualTo("/flags/bra.png");
        assertThat(response.logoUrl()).isEqualTo("/logos/bra.png");
        assertThat(response.fifaRanking()).isEqualTo(1);
        assertThat(response.managerName()).isEqualTo("Dorival Júnior");
        assertThat(response.stats()).isNotNull();
        assertThat(response.stats().wins()).isEqualTo(2);
    }

    @Test
    void teamResponse_from_withNullStats() {
        team.setStats(null);

        TeamResponse response = TeamResponse.from(team);

        assertThat(response.stats()).isNull();
    }

    // TeamDetailResponse.from()

    @Test
    void teamDetailResponse_from_mapsAllFieldsIncludingSquad() {
        TeamDetailResponse response = TeamDetailResponse.from(team);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.countryName()).isEqualTo("Brazil");
        assertThat(response.squad()).hasSize(1);
        assertThat(response.squad().get(0).name()).isEqualTo("Neymar Jr");
        assertThat(response.squad().get(0).isCaptain()).isTrue();
        assertThat(response.stats()).isNotNull();
    }

    @Test
    void teamDetailResponse_from_withNullSquad_returnsEmptyList() {
        team.setSquad(null);

        TeamDetailResponse response = TeamDetailResponse.from(team);

        assertThat(response.squad()).isEmpty();
    }

    @Test
    void teamDetailResponse_from_withEmptySquad_returnsEmptyList() {
        team.setSquad(List.of());

        TeamDetailResponse response = TeamDetailResponse.from(team);

        assertThat(response.squad()).isEmpty();
    }

    // EventResponse.from()

    private Event createEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setMatchNumber(1);
        event.setStage(Stage.GROUP);
        event.setGroupLetter(Group.A);
        event.setHomeTeam(team);

        Team awayTeam = new Team();
        awayTeam.setId(2L);
        awayTeam.setCountryName("Argentina");
        awayTeam.setCountryCode("ARG");
        awayTeam.setGroupLetter(Group.A);
        awayTeam.setCreatedAt(LocalDateTime.now());
        awayTeam.setUpdatedAt(LocalDateTime.now());
        event.setAwayTeam(awayTeam);

        event.setMatchDate(LocalDate.of(2026, 6, 11));
        event.setKickoffTime(LocalTime.of(20, 0));
        event.setKickoffUtc(LocalDateTime.of(2026, 6, 11, 20, 0));
        event.setArenaName("Lusail Stadium");
        event.setCity("Lusail");
        event.setStatus(MatchStatus.SCHEDULED);
        event.setHomeScore(null);
        event.setAwayScore(null);
        event.setWinnerTeam(null);
        event.setIsDraw(null);
        event.setHasExtraTime(false);
        event.setHasPenalties(false);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return event;
    }

    @Test
    void eventResponse_from_mapsAllFields() {
        Event event = createEvent();

        EventResponse response = EventResponse.from(event);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.matchNumber()).isEqualTo(1);
        assertThat(response.stage()).isEqualTo(Stage.GROUP);
        assertThat(response.groupLetter()).isEqualTo(Group.A);
        assertThat(response.homeTeam()).isNotNull();
        assertThat(response.homeTeam().countryName()).isEqualTo("Brazil");
        assertThat(response.awayTeam()).isNotNull();
        assertThat(response.awayTeam().countryName()).isEqualTo("Argentina");
        assertThat(response.arenaName()).isEqualTo("Lusail Stadium");
        assertThat(response.city()).isEqualTo("Lusail");
        assertThat(response.status()).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(response.winnerTeam()).isNull();
        assertThat(response.isDraw()).isNull();
        assertThat(response.hasExtraTime()).isFalse();
        assertThat(response.hasPenalties()).isFalse();
    }

    @Test
    void eventResponse_from_withNullTeams_returnsNullTeamResponses() {
        Event event = createEvent();
        event.setHomeTeam(null);
        event.setAwayTeam(null);

        EventResponse response = EventResponse.from(event);

        assertThat(response.homeTeam()).isNull();
        assertThat(response.awayTeam()).isNull();
    }

    @Test
    void eventResponse_from_withWinnerTeam_mapsWinner() {
        Event event = createEvent();
        event.setWinnerTeam(team);
        event.setHomeScore(2);
        event.setAwayScore(0);
        event.setIsDraw(false);
        event.setStatus(MatchStatus.FINISHED);

        EventResponse response = EventResponse.from(event);

        assertThat(response.winnerTeam()).isNotNull();
        assertThat(response.winnerTeam().countryName()).isEqualTo("Brazil");
        assertThat(response.homeScore()).isEqualTo(2);
        assertThat(response.awayScore()).isEqualTo(0);
        assertThat(response.isDraw()).isFalse();
    }
}
