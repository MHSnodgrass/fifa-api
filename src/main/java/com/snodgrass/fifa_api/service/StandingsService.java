package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.dto.response.BracketMatchResponse;
import com.snodgrass.fifa_api.dto.response.BracketStandingsResponse;
import com.snodgrass.fifa_api.dto.response.GroupStandingsResponse;
import com.snodgrass.fifa_api.dto.response.GroupStandingsTeamResponse;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.TeamStats;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.repository.EventRepository;
import com.snodgrass.fifa_api.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StandingsService {
    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;

    public List<GroupStandingsResponse> getGroupStandings() {
        List<Team> teams = teamRepository.findAll();
        Map<Group, List<Team>> grouped = teams.stream()
                .collect(Collectors.groupingBy(Team::getGroupLetter));

        Comparator<Team> standingsComparator = Comparator
                .comparingInt((Team t) -> safeStats(t).getGroupPoints()).reversed()
                .thenComparingInt((Team t) -> safeStats(t).getGoalDifference()).reversed()
                .thenComparingInt((Team t) -> safeStats(t).getGoalsFor()).reversed()
                .thenComparing(Team::getFifaRanking, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Team::getCountryName, Comparator.nullsLast(String::compareTo));

        return Stream.of(Group.values())
                .map(group -> new GroupStandingsResponse(
                        group,
                        grouped.getOrDefault(group, List.of()).stream()
                                .sorted(standingsComparator)
                                .map(GroupStandingsTeamResponse::from)
                                .toList()
                ))
                .toList();
    }

    public BracketStandingsResponse getBracketStandings() {
        List<Event> knockoutEvents = eventRepository.findByStageInOrderByMatchNumberAsc(List.of(
                Stage.ROUND_OF_32,
                Stage.ROUND_OF_16,
                Stage.QUARTERFINAL,
                Stage.SEMIFINAL,
                Stage.THIRD_PLACE,
                Stage.FINAL
        ));

        Map<Stage, List<BracketMatchResponse>> byStage = knockoutEvents.stream()
                .collect(Collectors.groupingBy(
                        Event::getStage,
                        Collectors.mapping(BracketMatchResponse::from, Collectors.toList())
                ));

        return new BracketStandingsResponse(
                byStage.getOrDefault(Stage.ROUND_OF_32, List.of()),
                byStage.getOrDefault(Stage.ROUND_OF_16, List.of()),
                byStage.getOrDefault(Stage.QUARTERFINAL, List.of()),
                byStage.getOrDefault(Stage.SEMIFINAL, List.of()),
                byStage.getOrDefault(Stage.THIRD_PLACE, List.of()),
                byStage.getOrDefault(Stage.FINAL, List.of())
        );
    }

    private TeamStats safeStats(Team team) {
        TeamStats stats = team.getStats();
        if (stats != null) {
            return stats;
        }
        return new TeamStats();
    }
}
