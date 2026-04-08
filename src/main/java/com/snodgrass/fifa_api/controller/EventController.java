package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.config.ApiHeaders;
import com.snodgrass.fifa_api.dto.request.EventRequest;
import com.snodgrass.fifa_api.dto.response.EventResponse;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.service.EventService;
import com.snodgrass.fifa_api.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Endpoints for retrieving World Cup match data")
public class EventController {
    private final EventService eventService;
    private final TeamService teamService;

    @Operation(summary = "Get all events")
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.debug("GET /api/events - Fetching all events");
        return ResponseEntity.ok(eventService.getAllEvents().stream().map(EventResponse::from).toList());
    }

    @Operation(summary = "Get event by ID")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @Parameter(description = "Event ID") @PathVariable Long id) {
        log.debug("GET /api/events/{} - Fetching event by id", id);
        return ResponseEntity.ok(EventResponse.from(eventService.getEventById(id)));
    }

    @Operation(summary = "Get events by group")
    @GetMapping("/group/{group}")
    public ResponseEntity<List<EventResponse>> getEventsByGroup(
            @Parameter(description = "Group letter (A-L)") @PathVariable Group group) {
        log.debug("GET /api/events/group/{} - Fetching events by group", group);
        return ResponseEntity.ok(eventService.getEventsByGroup(group).stream().map(EventResponse::from).toList());
    }

    @Operation(summary = "Get events by stage")
    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<EventResponse>> getEventsByStage(
            @Parameter(description = "Tournament stage") @PathVariable Stage stage) {
        log.debug("GET /api/events/stage/{} - Fetching events by stage", stage);
        return ResponseEntity.ok(eventService.getEventsByStage(stage).stream().map(EventResponse::from).toList());
    }

    @Operation(summary = "Get events by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponse>> getEventsByStatus(
            @Parameter(description = "Match status") @PathVariable MatchStatus status) {
        log.debug("GET /api/events/status/{} - Fetching events by status", status);
        return ResponseEntity.ok(eventService.getEventsByStatus(status).stream().map(EventResponse::from).toList());
    }

    @Operation(summary = "Get events by team")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<EventResponse>> getEventsByTeam(
            @Parameter(description = "Team ID") @PathVariable Long teamId) {
        log.debug("GET /api/events/team/{} - Fetching events by team id", teamId);
        return ResponseEntity.ok(eventService.getEventsByTeam(teamService.getTeamById(teamId)).stream().map(EventResponse::from).toList());
    }

    @Operation(summary = "Create a new event (test database only)",
            description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                    + "Requests without this header will be rejected with 403 Forbidden.",
            parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true,
                    description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "201", description = "Event created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        log.debug("POST /api/events - Creating event");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventResponse.from(eventService.createEvent(eventRequest)));
    }

    @Operation(summary = "Update an existing event (test database only)",
            description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                    + "Requests without this header will be rejected with 403 Forbidden.",
            parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true,
                    description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "200", description = "Event updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "Event ID") @PathVariable Long id,
            @Valid @RequestBody EventRequest eventRequest) {
        log.debug("PUT /api/events/{} - Updating event", id);
        return ResponseEntity.ok(EventResponse.from(eventService.updateEvent(id, eventRequest)));
    }

    @Operation(summary = "Delete an event (test database only)",
            description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                    + "Requests without this header will be rejected with 403 Forbidden.",
            parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true,
                    description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "204", description = "Event deleted successfully")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "Event ID") @PathVariable Long id) {
        log.debug("DELETE /api/events/{} - Deleting event", id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
