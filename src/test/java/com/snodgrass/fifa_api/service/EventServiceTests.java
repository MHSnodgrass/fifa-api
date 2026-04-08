package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.dto.request.EventRequest;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.repository.EventRepository;
import com.snodgrass.fifa_api.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTests {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private EventService eventService;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setMatchNumber(1);
        event.setStage(Stage.GROUP);
        event.setGroupLetter(Group.A);
        event.setArenaName("Lusail Stadium");
        event.setCity("Lusail");
        event.setMatchDate(LocalDate.of(2026, 6, 11));
        event.setStatus(MatchStatus.SCHEDULED);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllEvents_returnsAllEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<Event> result = eventService.getAllEvents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getArenaName()).isEqualTo("Lusail Stadium");
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getAllEvents_returnsEmptyList_whenNoEvents() {
        when(eventRepository.findAll()).thenReturn(List.of());

        List<Event> result = eventService.getAllEvents();

        assertThat(result).isEmpty();
    }

    @Test
    void getEventById_returnsEvent_whenFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.getEventById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMatchNumber()).isEqualTo(1);
    }

    @Test
    void getEventById_throwsEntityNotFoundException_whenNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getEventsByGroup_returnsEventsInGroup() {
        when(eventRepository.findByGroupLetter(Group.A)).thenReturn(List.of(event));

        List<Event> result = eventService.getEventsByGroup(Group.A);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getGroupLetter()).isEqualTo(Group.A);
    }

    @Test
    void getEventsByGroup_returnsEmptyList_whenNoEventsInGroup() {
        when(eventRepository.findByGroupLetter(Group.B)).thenReturn(List.of());

        List<Event> result = eventService.getEventsByGroup(Group.B);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsByStage_returnsEventsByStage() {
        when(eventRepository.findByStage(Stage.GROUP)).thenReturn(List.of(event));

        List<Event> result = eventService.getEventsByStage(Stage.GROUP);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStage()).isEqualTo(Stage.GROUP);
    }

    @Test
    void getEventsByStage_returnsEmptyList_whenNoEventsForStage() {
        when(eventRepository.findByStage(Stage.FINAL)).thenReturn(List.of());

        List<Event> result = eventService.getEventsByStage(Stage.FINAL);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsByStatus_returnsEventsByStatus() {
        when(eventRepository.findByStatus(MatchStatus.SCHEDULED)).thenReturn(List.of(event));

        List<Event> result = eventService.getEventsByStatus(MatchStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(MatchStatus.SCHEDULED);
    }

    @Test
    void getEventsByStatus_returnsEmptyList_whenNoEventsForStatus() {
        when(eventRepository.findByStatus(MatchStatus.FINISHED)).thenReturn(List.of());

        List<Event> result = eventService.getEventsByStatus(MatchStatus.FINISHED);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsByTeam_returnsMatchHistory() {
        Team team = new Team();
        team.setId(1L);
        when(eventRepository.findByHomeTeamOrAwayTeam(team, team)).thenReturn(List.of(event));

        List<Event> result = eventService.getEventsByTeam(team);

        assertThat(result).hasSize(1);
    }

    @Test
    void getEventsByTeam_returnsEmptyList_whenNoMatches() {
        Team team = new Team();
        team.setId(99L);
        when(eventRepository.findByHomeTeamOrAwayTeam(team, team)).thenReturn(List.of());

        List<Event> result = eventService.getEventsByTeam(team);

        assertThat(result).isEmpty();
    }

    // CUD helpers

    private EventRequest validEventRequest() {
        return new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
    }

    private EventRequest knockoutEventRequest() {
        return new EventRequest(49, Stage.QUARTERFINAL, null,
                null, null, "Winner Group A", "Runner-up Group B",
                LocalDate.of(2026, 7, 4), null, null,
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
    }

    private Team createTeam(Long id, String name) {
        Team t = new Team();
        t.setId(id);
        t.setCountryName(name);
        t.setCountryCode(name.substring(0, 3).toUpperCase());
        t.setGroupLetter(Group.A);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return t;
    }

    // createEvent

    @Test
    void createEvent_savesAndReturnsEvent() {
        EventRequest request = validEventRequest();
        Team home = createTeam(1L, "Brazil");
        Team away = createTeam(2L, "Argentina");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(home));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(away));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Event result = eventService.createEvent(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMatchNumber()).isEqualTo(1);
        assertThat(result.getHomeTeam()).isEqualTo(home);
        assertThat(result.getAwayTeam()).isEqualTo(away);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_withNullTeamIds_savesWithoutTeams() {
        EventRequest request = knockoutEventRequest();
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Event result = eventService.createEvent(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getHomeTeam()).isNull();
        assertThat(result.getAwayTeam()).isNull();
        assertThat(result.getHomeTeamPlaceholder()).isEqualTo("Winner Group A");
        verify(teamRepository, never()).findById(any());
    }

    @Test
    void createEvent_throwsEntityNotFoundException_whenHomeTeamNotFound() {
        EventRequest request = validEventRequest();
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Home team not found");

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_throwsEntityNotFoundException_whenAwayTeamNotFound() {
        EventRequest request = validEventRequest();
        Team home = createTeam(1L, "Brazil");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(home));
        when(teamRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Away team not found");

        verify(eventRepository, never()).save(any(Event.class));
    }

    // updateEvent

    @Test
    void updateEvent_savesAndReturnsUpdatedEvent() {
        EventRequest request = validEventRequest();
        Team home = createTeam(1L, "Brazil");
        Team away = createTeam(2L, "Argentina");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(home));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(away));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Event result = eventService.updateEvent(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMatchNumber()).isEqualTo(1);
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateEvent_throwsEntityNotFoundException_whenEventNotFound() {
        EventRequest request = validEventRequest();
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(eventRepository, never()).save(any(Event.class));
    }

    // deleteEvent

    @Test
    void deleteEvent_deletesEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(1L);

        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    void deleteEvent_throwsEntityNotFoundException_whenNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.deleteEvent(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(eventRepository, never()).delete(any(Event.class));
    }
}
