package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.config.ApiHeaders;
import com.snodgrass.fifa_api.dto.request.EventRequest;
import com.snodgrass.fifa_api.dto.request.SimulationRequest;
import com.snodgrass.fifa_api.dto.request.TeamRequest;
import com.snodgrass.fifa_api.dto.response.EventResponse;
import com.snodgrass.fifa_api.dto.response.ResetDbResponse;
import com.snodgrass.fifa_api.dto.response.SimulationResponse;
import com.snodgrass.fifa_api.dto.response.TeamDetailResponse;
import com.snodgrass.fifa_api.model.enums.ResetMode;
import com.snodgrass.fifa_api.service.EventService;
import com.snodgrass.fifa_api.service.TeamService;
import com.snodgrass.fifa_api.service.TestDatabaseResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "Endpoints for managing test database state")
public class TestController {
    private final TestDatabaseResetService resetService;
    private final TeamService teamService;
    private final EventService eventService;

    @Value("${app.tenant.test-schema}")
    private String testSchema;

    @Operation(summary = "Reset test database from template or production schema",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to allow this write operation.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "200", description = "Test database reset completed")
    @ApiResponse(responseCode = "400", description = "Invalid reset mode")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "500", description = "Reset operation failed")
    @PostMapping("/reset-db/{mode}")
    public ResponseEntity<ResetDbResponse> resetDatabase(@PathVariable ResetMode mode) {
        log.debug("POST /api/test/reset-db/{} - Resetting test database", mode);
        String sourceSchema = resetService.resetTestDatabase(mode);
        return ResponseEntity.ok(new ResetDbResponse(
                "Test database reset successfully",
                mode,
                sourceSchema,
                testSchema
        ));
    }

    @Operation(summary = "Create a new team (test database only)",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                + "Requests without this header will be rejected with 403 Forbidden.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "201", description = "Team created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @PostMapping("/teams")
    public ResponseEntity<TeamDetailResponse> createTeam(@Valid @RequestBody TeamRequest teamRequest) {
        log.debug("POST /api/test/teams - Creating team");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TeamDetailResponse.from(teamService.createTeam(teamRequest)));
    }

    @Operation(summary = "Update an existing team (test database only)",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                + "Requests without this header will be rejected with 403 Forbidden.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "200", description = "Team updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @ApiResponse(responseCode = "409", description = "Team has associated events")
    @PutMapping("/teams/{id}")
    public ResponseEntity<TeamDetailResponse> updateTeam(
            @Parameter(description = "Team ID") @PathVariable Long id,
            @Valid @RequestBody TeamRequest teamRequest) {
        log.debug("PUT /api/test/teams/{} - Updating team", id);
        return ResponseEntity.ok(TeamDetailResponse.from(teamService.updateTeam(id, teamRequest)));
    }

    @Operation(summary = "Delete a team (test database only)",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                + "Requests without this header will be rejected with 403 Forbidden.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "204", description = "Team deleted successfully")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @ApiResponse(responseCode = "409", description = "Team has associated events")
    @DeleteMapping("/teams/{id}")
    public ResponseEntity<Void> deleteTeam(@Parameter(description = "Team ID") @PathVariable Long id) {
        log.debug("DELETE /api/test/teams/{} - Deleting team", id);
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a new event (test database only)",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                + "Requests without this header will be rejected with 403 Forbidden.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "201", description = "Event created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        log.debug("POST /api/test/events - Creating event");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventResponse.from(eventService.createEvent(eventRequest)));
    }

    @Operation(summary = "Update an existing event (test database only)",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                + "Requests without this header will be rejected with 403 Forbidden.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "200", description = "Event updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @PutMapping("/events/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "Event ID") @PathVariable Long id,
            @Valid @RequestBody EventRequest eventRequest) {
        log.debug("PUT /api/test/events/{} - Updating event", id);
        return ResponseEntity.ok(EventResponse.from(eventService.updateEvent(id, eventRequest)));
    }

    @Operation(summary = "Delete an event (test database only)",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                + "Requests without this header will be rejected with 403 Forbidden.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "204", description = "Event deleted successfully")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@Parameter(description = "Event ID") @PathVariable Long id) {
        log.debug("DELETE /api/test/events/{} - Deleting event", id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Run a simulation (test database only)",
        description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database.",
        parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true, example = ApiHeaders.TEST_HEADER_VALUE,
                description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @PostMapping("/simulation/run")
    public ResponseEntity<SimulationResponse> runSimulation(@Valid @RequestBody SimulationRequest simulationRequest) {
        log.debug("POST /api/test/simulation/run - Running simulation");
        return ResponseEntity.ok(new SimulationResponse(
                simulationRequest.targetStage(),
                simulationRequest.completionStatus(),
                0,
                List.of(),
                simulationRequest.seed()
        ));
    }
}
