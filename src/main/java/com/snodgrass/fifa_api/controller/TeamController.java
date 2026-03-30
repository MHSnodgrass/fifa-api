package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.TeamResponse;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Endpoints for retrieving World Cup team data")
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "Get all teams")
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams().stream().map(TeamResponse::from).toList());
    }

    @Operation(summary = "Get team by ID")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(
            @Parameter(description = "Team ID") @PathVariable Long id) {
        return ResponseEntity.ok(TeamResponse.from(teamService.getTeamById(id)));
    }

    @Operation(summary = "Get teams by group")
    @GetMapping("/group/{group}")
    public ResponseEntity<List<TeamResponse>> getTeamsByGroup(
            @Parameter(description = "Group letter (A-L)") @PathVariable Group group) {
        return ResponseEntity.ok(teamService.getTeamsByGroup(group).stream().map(TeamResponse::from).toList());
    }
}
