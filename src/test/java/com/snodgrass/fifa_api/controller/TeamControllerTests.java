package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.request.PlayerRequest;
import com.snodgrass.fifa_api.dto.request.TeamRequest;
import com.snodgrass.fifa_api.dto.request.TeamStatsRequest;
import com.snodgrass.fifa_api.dto.response.TeamDetailResponse;
import com.snodgrass.fifa_api.dto.response.TeamResponse;
import com.snodgrass.fifa_api.exception.TeamHasEventsException;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.TeamPlayer;
import com.snodgrass.fifa_api.model.enums.Group;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TeamControllerTests {
    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    private Team team;

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
    }

    @Test
    void getAllTeams_returns200WithList() {
        when(teamService.getAllTeams()).thenReturn(List.of(team));

        ResponseEntity<List<TeamResponse>> response = teamController.getAllTeams();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().countryName()).isEqualTo("Brazil");
    }

    @Test
    void getAllTeams_returns200WithEmptyList_whenNoTeams() {
        when(teamService.getAllTeams()).thenReturn(List.of());

        ResponseEntity<List<TeamResponse>> response = teamController.getAllTeams();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getTeamById_returns200_whenFound() {
        when(teamService.getTeamById(1L)).thenReturn(team);

        ResponseEntity<TeamDetailResponse> response = teamController.getTeamById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().countryName()).isEqualTo("Brazil");
        assertThat(response.getBody().squad()).hasSize(1);
        assertThat(response.getBody().squad().getFirst().name()).isEqualTo("Neymar Jr");
        assertThat(response.getBody().squad().getFirst().isCaptain()).isTrue();
    }

    @Test
    void getTeamById_throwsEntityNotFoundException_whenNotFound() {
        when(teamService.getTeamById(99L)).thenThrow(new EntityNotFoundException("Team not found with id: 99"));

        assertThatThrownBy(() -> teamController.getTeamById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getTeamsByGroup_returns200WithList() {
        when(teamService.getTeamsByGroup(Group.A)).thenReturn(List.of(team));

        ResponseEntity<List<TeamResponse>> response = teamController.getTeamsByGroup(Group.A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().groupLetter()).isEqualTo(Group.A);
    }

    @Test
    void getTeamsByGroup_returns200WithEmptyList_whenNoTeamsInGroup() {
        when(teamService.getTeamsByGroup(Group.B)).thenReturn(List.of());

        ResponseEntity<List<TeamResponse>> response = teamController.getTeamsByGroup(Group.B);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // CUD helpers

    private TeamRequest validTeamRequest() {
        List<PlayerRequest> squad = List.of(
                new PlayerRequest("Neymar Jr", 10, "FW", true)
        );
        TeamStatsRequest stats = new TeamStatsRequest(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        return new TeamRequest("Brazil", "BRA", Group.A,
                "/flags/bra.png", "/logos/bra.png", 1, "Dorival Júnior", squad, stats);
    }

    // createTeam

    @Test
    void createTeam_returns201WithBody() {
        TeamRequest request = validTeamRequest();
        when(teamService.createTeam(any(TeamRequest.class))).thenReturn(team);

        ResponseEntity<TeamDetailResponse> response = teamController.createTeam(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().countryName()).isEqualTo("Brazil");
        verify(teamService, times(1)).createTeam(any(TeamRequest.class));
    }

    // updateTeam

    @Test
    void updateTeam_returns200WithBody() {
        TeamRequest request = validTeamRequest();
        when(teamService.updateTeam(eq(1L), any(TeamRequest.class))).thenReturn(team);

        ResponseEntity<TeamDetailResponse> response = teamController.updateTeam(1L, request);

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

        assertThatThrownBy(() -> teamController.updateTeam(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // deleteTeam

    @Test
    void deleteTeam_returns204() {
        doNothing().when(teamService).deleteTeam(1L);

        ResponseEntity<Void> response = teamController.deleteTeam(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(teamService, times(1)).deleteTeam(1L);
    }

    @Test
    void deleteTeam_throwsEntityNotFoundException_whenNotFound() {
        doThrow(new EntityNotFoundException("Team not found with id: 99"))
                .when(teamService).deleteTeam(99L);

        assertThatThrownBy(() -> teamController.deleteTeam(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteTeam_throwsTeamHasEventsException_whenTeamHasEvents() {
        doThrow(new TeamHasEventsException(1L))
                .when(teamService).deleteTeam(1L);

        assertThatThrownBy(() -> teamController.deleteTeam(1L))
                .isInstanceOf(TeamHasEventsException.class)
                .hasMessageContaining("1");
    }
}
