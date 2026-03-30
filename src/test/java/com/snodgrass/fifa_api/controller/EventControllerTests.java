package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.EventResponse;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.service.EventService;
import com.snodgrass.fifa_api.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTests {
    @Mock
    private EventService eventService;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private EventController eventController;

    private Team team;
    private Event event;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setId(1L);
        team.setCountryName("Brazil");
        team.setCountryCode("BRA");
        team.setGroupLetter(Group.A);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());

        event = new Event();
        event.setId(1L);
        event.setMatchNumber(1);
        event.setStage(Stage.GROUP);
        event.setGroupLetter(Group.A);
        event.setHomeTeam(team);
        event.setArenaName("Lusail Stadium");
        event.setCity("Lusail");
        event.setMatchDate(LocalDate.of(2026, 6, 11));
        event.setStatus(MatchStatus.SCHEDULED);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllEvents_returns200WithList() {
        when(eventService.getAllEvents()).thenReturn(List.of(event));

        ResponseEntity<List<EventResponse>> response = eventController.getAllEvents();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().arenaName()).isEqualTo("Lusail Stadium");
    }

    @Test
    void getAllEvents_returns200WithEmptyList_whenNoEvents() {
        when(eventService.getAllEvents()).thenReturn(List.of());

        ResponseEntity<List<EventResponse>> response = eventController.getAllEvents();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getEventById_returns200_whenFound() {
        when(eventService.getEventById(1L)).thenReturn(event);

        ResponseEntity<EventResponse> response = eventController.getEventById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().matchNumber()).isEqualTo(1);
    }

    @Test
    void getEventById_throwsEntityNotFoundException_whenNotFound() {
        when(eventService.getEventById(99L)).thenThrow(new EntityNotFoundException("Event not found with id: 99"));

        assertThatThrownBy(() -> eventController.getEventById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getEventsByGroup_returns200WithList() {
        when(eventService.getEventsByGroup(Group.A)).thenReturn(List.of(event));

        ResponseEntity<List<EventResponse>> response = eventController.getEventsByGroup(Group.A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().groupLetter()).isEqualTo(Group.A);
    }

    @Test
    void getEventsByGroup_returns200WithEmptyList_whenNoEventsInGroup() {
        when(eventService.getEventsByGroup(Group.B)).thenReturn(List.of());

        ResponseEntity<List<EventResponse>> response = eventController.getEventsByGroup(Group.B);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getEventsByStage_returns200WithList() {
        when(eventService.getEventsByStage(Stage.GROUP)).thenReturn(List.of(event));

        ResponseEntity<List<EventResponse>> response = eventController.getEventsByStage(Stage.GROUP);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().stage()).isEqualTo(Stage.GROUP);
    }

    @Test
    void getEventsByStage_returns200WithEmptyList_whenNoEventsForStage() {
        when(eventService.getEventsByStage(Stage.FINAL)).thenReturn(List.of());

        ResponseEntity<List<EventResponse>> response = eventController.getEventsByStage(Stage.FINAL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getEventsByStatus_returns200WithList() {
        when(eventService.getEventsByStatus(MatchStatus.SCHEDULED)).thenReturn(List.of(event));

        ResponseEntity<List<EventResponse>> response = eventController.getEventsByStatus(MatchStatus.SCHEDULED);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().status()).isEqualTo(MatchStatus.SCHEDULED);
    }

    @Test
    void getEventsByStatus_returns200WithEmptyList_whenNoEventsForStatus() {
        when(eventService.getEventsByStatus(MatchStatus.FINISHED)).thenReturn(List.of());

        ResponseEntity<List<EventResponse>> response = eventController.getEventsByStatus(MatchStatus.FINISHED);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getEventsByTeam_returns200WithList() {
        when(teamService.getTeamById(1L)).thenReturn(team);
        when(eventService.getEventsByTeam(team)).thenReturn(List.of(event));

        ResponseEntity<List<EventResponse>> response = eventController.getEventsByTeam(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getEventsByTeam_throwsEntityNotFoundException_whenTeamNotFound() {
        when(teamService.getTeamById(99L)).thenThrow(new EntityNotFoundException("Team not found with id: 99"));

        assertThatThrownBy(() -> eventController.getEventsByTeam(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
