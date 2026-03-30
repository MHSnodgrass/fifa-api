package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

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
}
