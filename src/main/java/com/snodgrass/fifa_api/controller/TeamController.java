package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.TeamResponse;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams().stream().map(TeamResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(TeamResponse.from(teamService.getTeamById(id)));
    }

    @GetMapping("/group/{group}")
    public ResponseEntity<List<TeamResponse>> getTeamsByGroup(@PathVariable Group group) {
        return ResponseEntity.ok(teamService.getTeamsByGroup(group).stream().map(TeamResponse::from).toList());
    }
}
