package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.TeamDetailResponse;
import com.snodgrass.fifa_api.dto.TeamResponse;
import com.snodgrass.fifa_api.model.Team;
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
import static org.mockito.Mockito.when;

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
        team.setSquad("[{\"name\":\"Neymar Jr\",\"number\":10,\"position\":\"FW\",\"isCaptain\":true}]");
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
}
