package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.config.ApiHeaders;
import com.snodgrass.fifa_api.dto.request.TeamRequest;
import com.snodgrass.fifa_api.dto.response.TeamDetailResponse;
import com.snodgrass.fifa_api.dto.response.TeamResponse;
import com.snodgrass.fifa_api.model.enums.Group;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Endpoints for retrieving World Cup team data")
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "Get all teams")
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        log.debug("GET /api/teams - Fetching all teams");
        return ResponseEntity.ok(teamService.getAllTeams().stream().map(TeamResponse::from).toList());
    }

    @Operation(summary = "Get team details by ID (includes squad information")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponse> getTeamById(
            @Parameter(description = "Team ID") @PathVariable Long id) {
        log.debug("GET /api/teams/{} - Fetching team by id", id);
        return ResponseEntity.ok(TeamDetailResponse.from(teamService.getTeamById(id)));
    }

    @Operation(summary = "Get teams by group")
    @GetMapping("/group/{group}")
    public ResponseEntity<List<TeamResponse>> getTeamsByGroup(
            @Parameter(description = "Group letter (A-L)") @PathVariable Group group) {
        log.debug("GET /api/teams/group/{} - Fetching teams by group", group);
        return ResponseEntity.ok(teamService.getTeamsByGroup(group).stream().map(TeamResponse::from).toList());
    }

    @Operation(summary = "Create a new team (test database only)",
            description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                    + "Requests without this header will be rejected with 403 Forbidden.",
            parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true,
                    description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "201", description = "Team created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @PostMapping
    public ResponseEntity<TeamDetailResponse> createTeam(@Valid @RequestBody TeamRequest teamRequest) {
        log.debug("POST /api/teams - Creating team");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TeamDetailResponse.from(teamService.createTeam(teamRequest)));
    }

    @Operation(summary = "Update an existing team (test database only)",
            description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                    + "Requests without this header will be rejected with 403 Forbidden.",
            parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true,
                    description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "200", description = "Team updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @PutMapping("/{id}")
    public ResponseEntity<TeamDetailResponse> updateTeam(
            @Parameter(description = "Team ID") @PathVariable Long id,
            @Valid @RequestBody TeamRequest teamRequest) {
        log.debug("PUT /api/teams/{} - Updating team", id);
        return ResponseEntity.ok(TeamDetailResponse.from(teamService.updateTeam(id, teamRequest)));
    }

    @Operation(summary = "Delete a team (test database only)",
            description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to target the test database. "
                    + "Requests without this header will be rejected with 403 Forbidden.",
            parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true,
                    description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "204", description = "Team deleted successfully")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @ApiResponse(responseCode = "409", description = "Team has associated events")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "Team ID") @PathVariable Long id) {
        log.debug("DELETE /api/teams/{} - Deleting team", id);
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}
