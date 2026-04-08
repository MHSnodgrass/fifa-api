package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.dto.request.PlayerRequest;
import com.snodgrass.fifa_api.dto.request.TeamRequest;
import com.snodgrass.fifa_api.dto.request.TeamStatsRequest;
import com.snodgrass.fifa_api.exception.TeamHasEventsException;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.repository.EventRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTests {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EventRepository eventRepository;

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

    // createTeam

    private TeamRequest validTeamRequest() {
        List<PlayerRequest> squad = List.of(
                new PlayerRequest("Neymar Jr", 10, "FW", true)
        );
        TeamStatsRequest stats = new TeamStatsRequest(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        return new TeamRequest("Brazil", "BRA", Group.A,
                "/flags/bra.png", "/logos/bra.png", 1, "Dorival Júnior", squad, stats);
    }

    @Test
    void createTeam_savesAndReturnsTeam() {
        TeamRequest request = validTeamRequest();
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Team result = teamService.createTeam(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCountryName()).isEqualTo("Brazil");
        assertThat(result.getCountryCode()).isEqualTo("BRA");
        assertThat(result.getGroupLetter()).isEqualTo(Group.A);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    // updateTeam

    @Test
    void updateTeam_savesAndReturnsUpdatedTeam() {
        TeamRequest request = validTeamRequest();
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Team result = teamService.updateTeam(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCountryName()).isEqualTo("Brazil");
        verify(teamRepository, times(1)).findById(1L);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    void updateTeam_throwsEntityNotFoundException_whenNotFound() {
        TeamRequest request = validTeamRequest();
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.updateTeam(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(teamRepository, never()).save(any(Team.class));
    }

    // deleteTeam

    @Test
    void deleteTeam_deletesTeam_whenNoAssociatedEvents() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(eventRepository.existsByHomeTeamOrAwayTeam(team, team)).thenReturn(false);

        teamService.deleteTeam(1L);

        verify(teamRepository, times(1)).delete(team);
    }

    @Test
    void deleteTeam_throwsEntityNotFoundException_whenNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.deleteTeam(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(teamRepository, never()).delete(any(Team.class));
    }

    @Test
    void deleteTeam_throwsTeamHasEventsException_whenTeamHasEvents() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(eventRepository.existsByHomeTeamOrAwayTeam(team, team)).thenReturn(true);

        assertThatThrownBy(() -> teamService.deleteTeam(1L))
                .isInstanceOf(TeamHasEventsException.class)
                .hasMessageContaining("1");

        verify(teamRepository, never()).delete(any(Team.class));
    }
}
