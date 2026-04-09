package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.dto.response.BracketStandingsResponse;
import com.snodgrass.fifa_api.dto.response.GroupStandingsResponse;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.TeamStats;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.repository.EventRepository;
import com.snodgrass.fifa_api.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandingsServiceTests {
    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private StandingsService standingsService;

    @Test
    void getGroupStandings_sortsByStatsThenFifaRankingFallback() {
        Team a = createTeam(1L, "Alpha", Group.A, 9, 1, 2, 5);
        Team b = createTeam(2L, "Beta", Group.A, 4, 1, 2, 10);
        Team c = createTeam(3L, "Gamma", Group.A, 4, 1, 2, 30);

        when(teamRepository.findAll()).thenReturn(List.of(c, a, b));

        List<GroupStandingsResponse> result = standingsService.getGroupStandings();

        GroupStandingsResponse groupA = result.stream()
                .filter(g -> g.groupLetter() == Group.A)
                .findFirst()
                .orElseThrow();

        assertThat(groupA.teams()).hasSize(3);
        assertThat(groupA.teams().getFirst().countryName()).isEqualTo("Alpha");
        assertThat(groupA.teams().get(1).countryName()).isEqualTo("Beta");
        assertThat(groupA.teams().get(2).countryName()).isEqualTo("Gamma");
    }

    @Test
    void getGroupStandings_returnsGroupsInAlphabeticalOrder() {
        when(teamRepository.findAll()).thenReturn(List.of(
                createTeam(1L, "Team L", Group.L, 1, 0, 0, 10),
                createTeam(2L, "Team A", Group.A, 1, 0, 0, 20)
        ));

        List<GroupStandingsResponse> result = standingsService.getGroupStandings();

        assertThat(result).hasSize(12);
        assertThat(result.getFirst().groupLetter()).isEqualTo(Group.A);
        assertThat(result.getLast().groupLetter()).isEqualTo(Group.L);
    }

    @Test
    void getBracketStandings_partitionsByRoundAndUsesPlaceholders() {
        Event r32 = createEvent(101L, 73, Stage.ROUND_OF_32, null, null, "1st Group A", "2nd Group B");
        Event finalMatch = createEvent(201L, 104, Stage.FINAL, null, null, "Winner SF-101", "Winner SF-102");
        when(eventRepository.findByStageInOrderByMatchNumberAsc(List.of(
                Stage.ROUND_OF_32,
                Stage.ROUND_OF_16,
                Stage.QUARTERFINAL,
                Stage.SEMIFINAL,
                Stage.THIRD_PLACE,
                Stage.FINAL
        ))).thenReturn(List.of(r32, finalMatch));

        BracketStandingsResponse response = standingsService.getBracketStandings();

        assertThat(response.roundOf32()).hasSize(1);
        assertThat(response.finalStage()).hasSize(1);
        assertThat(response.roundOf32().getFirst().homeTeamName()).isEqualTo("1st Group A");
        assertThat(response.finalStage().getFirst().awayTeamName()).isEqualTo("Winner SF-102");
    }

    private Team createTeam(Long id, String name, Group group, int points, int gd, int gf, Integer fifaRanking) {
        Team team = new Team();
        team.setId(id);
        team.setCountryName(name);
        team.setCountryCode(name.substring(0, Math.min(3, name.length())).toUpperCase());
        team.setGroupLetter(group);
        team.setFifaRanking(fifaRanking);

        TeamStats stats = new TeamStats();
        stats.setGroupPoints(points);
        stats.setGoalDifference(gd);
        stats.setGoalsFor(gf);
        team.setStats(stats);
        return team;
    }

    private Event createEvent(Long id, int matchNumber, Stage stage, Team home, Team away, String homePlaceholder, String awayPlaceholder) {
        Event e = new Event();
        e.setId(id);
        e.setMatchNumber(matchNumber);
        e.setStage(stage);
        e.setStatus(MatchStatus.SCHEDULED);
        e.setArenaName("Arena");
        e.setCity("City");
        e.setHomeTeam(home);
        e.setAwayTeam(away);
        e.setHomeTeamPlaceholder(homePlaceholder);
        e.setAwayTeamPlaceholder(awayPlaceholder);
        return e;
    }
}
