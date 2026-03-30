package com.snodgrass.fifa_api.repository;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class EventRepositoryTests {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void shouldLoadEvents() {
        List<Event> events = eventRepository.findAll();
        assertFalse(events.isEmpty());
    }

    @Test
    void findByGroupLetter_returnsEventsInGroup() {
        List<Event> events = eventRepository.findByGroupLetter(Group.A);
        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(e -> e.getGroupLetter() == Group.A);
    }

    @Test
    void findByStage_returnsEventsByStage() {
        List<Event> events = eventRepository.findByStage(Stage.GROUP);
        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(e -> e.getStage() == Stage.GROUP);
    }

    @Test
    void findByStatus_returnsEventsByStatus() {
        List<Event> events = eventRepository.findByStatus(MatchStatus.SCHEDULED);
        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(e -> e.getStatus() == MatchStatus.SCHEDULED);
    }

    @Test
    void findByHomeTeamOrAwayTeam_returnsMatchHistory() {
        Team team = teamRepository.findAll().getFirst();
        List<Event> events = eventRepository.findByHomeTeamOrAwayTeam(team, team);
        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(e ->
                e.getHomeTeam().getId().equals(team.getId()) ||
                e.getAwayTeam().getId().equals(team.getId())
        );
    }
}
