package com.snodgrass.fifa_api.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ScheduleResponse(
        LocalDate date,
        List<ScheduleEventResponse> scheduledEvents
) {}
