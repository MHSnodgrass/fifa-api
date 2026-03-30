package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.service.EventService;
import com.snodgrass.fifa_api.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/group/{group}")
    public ResponseEntity<List<Event>> getEventsByGroup(@PathVariable Group group) {
        return ResponseEntity.ok(eventService.getEventsByGroup(group));
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<Event>> getEventsByStage(@PathVariable Stage stage) {
        return ResponseEntity.ok(eventService.getEventsByStage(stage));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Event>> getEventsByStatus(@PathVariable MatchStatus status) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Event>> getEventsByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(eventService.getEventsByTeam(teamService.getTeamById(teamId)));
    }
}
