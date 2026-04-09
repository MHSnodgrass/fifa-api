package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.response.ScheduleEventResponse;
import com.snodgrass.fifa_api.dto.response.ScheduleResponse;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTests {
    @Mock
    private EventService eventService;

    @InjectMocks
    private ScheduleController scheduleController;

    @Test
    void getSchedule_returns200_whenRangeValid() {
        LocalDate start = LocalDate.of(2026, 6, 11);
        LocalDate end = LocalDate.of(2026, 6, 12);
        List<ScheduleResponse> payload = List.of(
                new ScheduleResponse(
                        start,
                        List.of(new ScheduleEventResponse(1L, "Arena", MatchStatus.SCHEDULED, null, null,
                                "Away", "/flags/away.png", "Home", "/flags/home.png"))
                )
        );
        when(eventService.getSchedule(start, end)).thenReturn(payload);

        ResponseEntity<List<ScheduleResponse>> response = scheduleController.getSchedule(start, end);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(eventService, times(1)).getSchedule(start, end);
    }

    @Test
    void getSchedule_throwsIllegalArgument_whenStartAfterEnd() {
        LocalDate start = LocalDate.of(2026, 6, 12);
        LocalDate end = LocalDate.of(2026, 6, 11);

        assertThatThrownBy(() -> scheduleController.getSchedule(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startDate");

        verify(eventService, never()).getSchedule(any(), any());
    }
}
