package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.EventResponse;
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
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents().stream().map(EventResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.getEventById(id)));
    }

    @GetMapping("/group/{group}")
    public ResponseEntity<List<EventResponse>> getEventsByGroup(@PathVariable Group group) {
        return ResponseEntity.ok(eventService.getEventsByGroup(group).stream().map(EventResponse::from).toList());
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<EventResponse>> getEventsByStage(@PathVariable Stage stage) {
        return ResponseEntity.ok(eventService.getEventsByStage(stage).stream().map(EventResponse::from).toList());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponse>> getEventsByStatus(@PathVariable MatchStatus status) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status).stream().map(EventResponse::from).toList());
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<EventResponse>> getEventsByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(eventService.getEventsByTeam(teamService.getTeamById(teamId)).stream().map(EventResponse::from).toList());
    }
}
