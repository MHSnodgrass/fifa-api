package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.response.TeamDetailResponse;
import com.snodgrass.fifa_api.dto.response.TeamResponse;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
