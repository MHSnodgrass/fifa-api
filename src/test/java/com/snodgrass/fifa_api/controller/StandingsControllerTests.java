package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.response.BracketStandingsResponse;
import com.snodgrass.fifa_api.dto.response.GroupStandingsResponse;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.service.StandingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandingsControllerTests {
    @Mock
    private StandingsService standingsService;

    @InjectMocks
    private StandingsController standingsController;

    @Test
    void getGroupStandings_returns200() {
        when(standingsService.getGroupStandings()).thenReturn(List.of(
                new GroupStandingsResponse(Group.A, List.of())
        ));

        ResponseEntity<List<GroupStandingsResponse>> response = standingsController.getGroupStandings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(standingsService, times(1)).getGroupStandings();
    }

    @Test
    void getBracketStandings_returns200() {
        when(standingsService.getBracketStandings()).thenReturn(new BracketStandingsResponse(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        ));

        ResponseEntity<BracketStandingsResponse> response = standingsController.getBracketStandings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(standingsService, times(1)).getBracketStandings();
    }
}
