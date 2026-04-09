package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.dto.request.EventRequest;
import com.snodgrass.fifa_api.dto.response.ScheduleEventResponse;
import com.snodgrass.fifa_api.dto.response.ScheduleResponse;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.repository.EventRepository;
import com.snodgrass.fifa_api.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
    }

    public List<Event> getEventsByGroup(Group group) {
        return eventRepository.findByGroupLetter(group);
    }

    public List<Event> getEventsByStage(Stage stage) {
        return eventRepository.findByStage(stage);
    }

    public List<Event> getEventsByStatus(MatchStatus status) {
        return eventRepository.findByStatus(status);
    }

    public List<Event> getEventsByTeam(Team team) {
        return eventRepository.findByHomeTeamOrAwayTeam(team, team);
    }

    public List<ScheduleResponse> getSchedule(LocalDate startDate, LocalDate endDate) {
        List<Event> events = eventRepository.findByMatchDateBetweenOrderByMatchDateAscKickoffTimeAsc(startDate, endDate);
        Map<LocalDate, List<Event>> grouped = events.stream().collect(groupingBy(
                Event::getMatchDate,
                LinkedHashMap::new,
                Collectors.toList()
        ));

        return grouped.entrySet().stream()
                .map(entry -> new ScheduleResponse(
                        entry.getKey(),
                        entry.getValue().stream().map(ScheduleEventResponse::from).toList()
                ))
                .toList();
    }

    public Event createEvent(EventRequest request) {
        Event event = request.toEntity();
        resolveTeamReferences(event, request);
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, EventRequest request) {
        eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
        Event event = request.toEntity();
        event.setId(id);
        resolveTeamReferences(event, request);
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
        eventRepository.delete(event);
    }

    private void resolveTeamReferences(Event event, EventRequest request) {
        if (request.homeTeamId() != null) {
            event.setHomeTeam(teamRepository.findById(request.homeTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Home team not found with id: " + request.homeTeamId())));
        }
        if (request.awayTeamId() != null) {
            event.setAwayTeam(teamRepository.findById(request.awayTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Away team not found with id: " + request.awayTeamId())));
        }
        if (request.winnerTeamId() != null) {
            event.setWinnerTeam(teamRepository.findById(request.winnerTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Winner team not found with id: " + request.winnerTeamId())));
        }
    }
}
