package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.request.EventRequest;
import com.snodgrass.fifa_api.dto.request.PlayerRequest;
import com.snodgrass.fifa_api.dto.request.TeamRequest;
import com.snodgrass.fifa_api.dto.request.TeamStatsRequest;
import com.snodgrass.fifa_api.dto.response.EventResponse;
import com.snodgrass.fifa_api.dto.response.ResetDbResponse;
import com.snodgrass.fifa_api.dto.response.TeamDetailResponse;
import com.snodgrass.fifa_api.exception.TeamHasEventsException;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.TeamPlayer;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.ResetMode;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.service.EventService;
import com.snodgrass.fifa_api.service.TeamService;
import com.snodgrass.fifa_api.service.TestDatabaseResetService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestControllerTests {
    @Mock
    private TestDatabaseResetService resetService;

    @Mock
    private TeamService teamService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private TestController testController;

    private Team team;
    private Event event;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setId(1L);
        team.setCountryName("Brazil");
        team.setCountryCode("BRA");
        team.setGroupLetter(Group.A);
        team.setSquad(List.of(new TeamPlayer("Neymar Jr", 10, "FW", true)));
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
    void resetDatabase_template_returns200() {
        when(resetService.resetTestDatabase(ResetMode.TEMPLATE)).thenReturn("fifa_world_cup_template");

        ResponseEntity<ResetDbResponse> response = testController.resetDatabase(ResetMode.TEMPLATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().mode()).isEqualTo(ResetMode.TEMPLATE);
        assertThat(response.getBody().sourceSchema()).isEqualTo("fifa_world_cup_template");
        verify(resetService, times(1)).resetTestDatabase(ResetMode.TEMPLATE);
    }

    @Test
    void resetDatabase_prodSync_returns200() {
        when(resetService.resetTestDatabase(ResetMode.PROD_SYNC)).thenReturn("fifa_world_cup");

        ResponseEntity<ResetDbResponse> response = testController.resetDatabase(ResetMode.PROD_SYNC);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().mode()).isEqualTo(ResetMode.PROD_SYNC);
        assertThat(response.getBody().sourceSchema()).isEqualTo("fifa_world_cup");
        verify(resetService, times(1)).resetTestDatabase(ResetMode.PROD_SYNC);
    }

    private TeamRequest validTeamRequest() {
        List<PlayerRequest> squad = List.of(
                new PlayerRequest("Neymar Jr", 10, "FW", true)
        );
        TeamStatsRequest stats = new TeamStatsRequest(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        return new TeamRequest("Brazil", "BRA", Group.A,
                "/flags/bra.png", "/logos/bra.png", 1, "Dorival Júnior", squad, stats);
    }

    private EventRequest validEventRequest() {
        return new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
    }

    @Test
    void createTeam_returns201WithBody() {
        TeamRequest request = validTeamRequest();
        when(teamService.createTeam(any(TeamRequest.class))).thenReturn(team);

        ResponseEntity<TeamDetailResponse> response = testController.createTeam(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().countryName()).isEqualTo("Brazil");
        verify(teamService, times(1)).createTeam(any(TeamRequest.class));
    }

    @Test
    void updateTeam_returns200WithBody() {
        TeamRequest request = validTeamRequest();
        when(teamService.updateTeam(eq(1L), any(TeamRequest.class))).thenReturn(team);

        ResponseEntity<TeamDetailResponse> response = testController.updateTeam(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().countryName()).isEqualTo("Brazil");
        verify(teamService, times(1)).updateTeam(eq(1L), any(TeamRequest.class));
    }

    @Test
    void updateTeam_throwsEntityNotFoundException_whenNotFound() {
        TeamRequest request = validTeamRequest();
        when(teamService.updateTeam(eq(99L), any(TeamRequest.class)))
                .thenThrow(new EntityNotFoundException("Team not found with id: 99"));

        assertThatThrownBy(() -> testController.updateTeam(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteTeam_returns204() {
        doNothing().when(teamService).deleteTeam(1L);

        ResponseEntity<Void> response = testController.deleteTeam(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(teamService, times(1)).deleteTeam(1L);
    }

    @Test
    void deleteTeam_throwsEntityNotFoundException_whenNotFound() {
        doThrow(new EntityNotFoundException("Team not found with id: 99"))
                .when(teamService).deleteTeam(99L);

        assertThatThrownBy(() -> testController.deleteTeam(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteTeam_throwsTeamHasEventsException_whenTeamHasEvents() {
        doThrow(new TeamHasEventsException(1L))
                .when(teamService).deleteTeam(1L);

        assertThatThrownBy(() -> testController.deleteTeam(1L))
                .isInstanceOf(TeamHasEventsException.class)
                .hasMessageContaining("1");
    }

    @Test
    void createEvent_returns201WithBody() {
        EventRequest request = validEventRequest();
        when(eventService.createEvent(any(EventRequest.class))).thenReturn(event);

        ResponseEntity<EventResponse> response = testController.createEvent(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().matchNumber()).isEqualTo(1);
        verify(eventService, times(1)).createEvent(any(EventRequest.class));
    }

    @Test
    void updateEvent_returns200WithBody() {
        EventRequest request = validEventRequest();
        when(eventService.updateEvent(eq(1L), any(EventRequest.class))).thenReturn(event);

        ResponseEntity<EventResponse> response = testController.updateEvent(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().matchNumber()).isEqualTo(1);
        verify(eventService, times(1)).updateEvent(eq(1L), any(EventRequest.class));
    }

    @Test
    void updateEvent_throwsEntityNotFoundException_whenNotFound() {
        EventRequest request = validEventRequest();
        when(eventService.updateEvent(eq(99L), any(EventRequest.class)))
                .thenThrow(new EntityNotFoundException("Event not found with id: 99"));

        assertThatThrownBy(() -> testController.updateEvent(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteEvent_returns204() {
        doNothing().when(eventService).deleteEvent(1L);

        ResponseEntity<Void> response = testController.deleteEvent(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(eventService, times(1)).deleteEvent(1L);
    }

    @Test
    void deleteEvent_throwsEntityNotFoundException_whenNotFound() {
        doThrow(new EntityNotFoundException("Event not found with id: 99"))
                .when(eventService).deleteEvent(99L);

        assertThatThrownBy(() -> testController.deleteEvent(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
