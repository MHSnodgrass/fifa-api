package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTests {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamService teamService;

    private Team team;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setId(1L);
        team.setCountryName("Brazil");
        team.setCountryCode("BRA");
        team.setGroupLetter(Group.A);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllTeams_returnsAllTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(team));

        List<Team> result = teamService.getAllTeams();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCountryName()).isEqualTo("Brazil");
        verify(teamRepository, times(1)).findAll();
    }

    @Test
    void getAllTeams_returnsEmptyList_whenNoTeams() {
        when(teamRepository.findAll()).thenReturn(List.of());

        List<Team> result = teamService.getAllTeams();

        assertThat(result).isEmpty();
    }

    @Test
    void getTeamById_returnsTeam_whenFound() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        Team result = teamService.getTeamById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCountryName()).isEqualTo("Brazil");
    }

    @Test
    void getTeamById_throwsEntityNotFoundException_whenNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getTeamsByGroup_returnsTeamsInGroup() {
        when(teamRepository.findByGroupLetter(Group.A)).thenReturn(List.of(team));

        List<Team> result = teamService.getTeamsByGroup(Group.A);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getGroupLetter()).isEqualTo(Group.A);
    }

    @Test
    void getTeamsByGroup_returnsEmptyList_whenNoTeamsInGroup() {
        when(teamRepository.findByGroupLetter(Group.B)).thenReturn(List.of());

        List<Team> result = teamService.getTeamsByGroup(Group.B);

        assertThat(result).isEmpty();
    }
}
