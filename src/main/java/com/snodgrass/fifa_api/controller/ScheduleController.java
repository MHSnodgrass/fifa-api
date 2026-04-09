package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.response.ScheduleResponse;
import com.snodgrass.fifa_api.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "Endpoints for retrieving schedule-focused event summaries")
public class ScheduleController {
    private final EventService eventService;

    @Operation(summary = "Get schedule by date range",
            description = "Returns events grouped by date with a lightweight event payload for schedule pages.")
    @ApiResponse(responseCode = "200", description = "Schedule returned successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or malformed date range")
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedule(
            @Parameter(description = "Start date (inclusive), format: YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive), format: YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        log.debug("GET /api/schedule?startDate={}&endDate={} - Fetching grouped schedule", startDate, endDate);
        return ResponseEntity.ok(eventService.getSchedule(startDate, endDate));
    }
}
