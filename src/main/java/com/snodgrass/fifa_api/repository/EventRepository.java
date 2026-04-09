package com.snodgrass.fifa_api.repository;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByGroupLetter(Group group);
    List<Event> findByStage(Stage stage);
    List<Event> findByStatus(MatchStatus status);
    List<Event> findByHomeTeamOrAwayTeam(Team homeTeam, Team awayTeam);
    List<Event> findByMatchDateBetweenOrderByMatchDateAscKickoffTimeAsc(LocalDate startDate, LocalDate endDate);
    List<Event> findByStageInOrderByMatchNumberAsc(List<Stage> stages);
    boolean existsByHomeTeamOrAwayTeam(Team homeTeam, Team awayTeam);
}
