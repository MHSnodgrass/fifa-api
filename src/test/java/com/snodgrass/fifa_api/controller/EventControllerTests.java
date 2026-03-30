package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import com.snodgrass.fifa_api.service.EventService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTests {
    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setMatchNumber(1);
        event.setStage(Stage.GROUP);
        event.setArenaName("Lusail Stadium");
        event.setCity("Lusail");
        event.setMatchDate(LocalDate.of(2026, 6, 11));
        event.setStatus(MatchStatus.SCHEDULED);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllEvents_returns200WithList() {
        when(eventService.getAllEvents()).thenReturn(List.of(event));

        ResponseEntity<List<Event>> response = eventController.getAllEvents();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().getArenaName()).isEqualTo("Lusail Stadium");
    }

    @Test
    void getAllEvents_returns200WithEmptyList_whenNoEvents() {
        when(eventService.getAllEvents()).thenReturn(List.of());

        ResponseEntity<List<Event>> response = eventController.getAllEvents();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getEventById_returns200_whenFound() {
        when(eventService.getEventById(1L)).thenReturn(event);

        ResponseEntity<Event> response = eventController.getEventById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getMatchNumber()).isEqualTo(1);
    }

    @Test
    void getEventById_throwsEntityNotFoundException_whenNotFound() {
        when(eventService.getEventById(99L)).thenThrow(new EntityNotFoundException("Event not found with id: 99"));

        assertThatThrownBy(() -> eventController.getEventById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
