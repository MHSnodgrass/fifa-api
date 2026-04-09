package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.response.BracketStandingsResponse;
import com.snodgrass.fifa_api.dto.response.GroupStandingsResponse;
import com.snodgrass.fifa_api.service.StandingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/standings")
@RequiredArgsConstructor
@Tag(name = "Standings", description = "Endpoints for group standings and knockout bracket views")
public class StandingsController {
    private final StandingsService standingsService;

    @Operation(summary = "Get group stage standings")
    @ApiResponse(responseCode = "200", description = "Group standings returned successfully")
    @GetMapping("/group")
    public ResponseEntity<List<GroupStandingsResponse>> getGroupStandings() {
        log.debug("GET /api/standings/group - Fetching grouped standings");
        return ResponseEntity.ok(standingsService.getGroupStandings());
    }

    @Operation(summary = "Get knockout bracket standings")
    @ApiResponse(responseCode = "200", description = "Bracket standings returned successfully")
    @GetMapping("/bracket")
    public ResponseEntity<BracketStandingsResponse> getBracketStandings() {
        log.debug("GET /api/standings/bracket - Fetching bracket standings");
        return ResponseEntity.ok(standingsService.getBracketStandings());
    }
}
