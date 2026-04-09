package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.dto.response.ResetDbResponse;
import com.snodgrass.fifa_api.model.enums.ResetMode;
import com.snodgrass.fifa_api.service.TestDatabaseResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestControllerTests {
    @Mock
    private TestDatabaseResetService resetService;

    @InjectMocks
    private TestController testController;

    @Test
    void resetDatabase_template_returns200() {
        when(resetService.resetTestDatabase(ResetMode.TEMPLATE)).thenReturn("fifa_world_cup_template");

        ResponseEntity<ResetDbResponse> response = testController.resetDatabase(ResetMode.TEMPLATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().mode()).isEqualTo(ResetMode.TEMPLATE);
        assertThat(response.getBody().sourceSchema()).isEqualTo("fifa_world_cup_template");
        verify(resetService, times(1)).resetTestDatabase(ResetMode.TEMPLATE);
    }

    @Test
    void resetDatabase_prodSync_returns200() {
        when(resetService.resetTestDatabase(ResetMode.PROD_SYNC)).thenReturn("fifa_world_cup");

        ResponseEntity<ResetDbResponse> response = testController.resetDatabase(ResetMode.PROD_SYNC);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().mode()).isEqualTo(ResetMode.PROD_SYNC);
        assertThat(response.getBody().sourceSchema()).isEqualTo("fifa_world_cup");
        verify(resetService, times(1)).resetTestDatabase(ResetMode.PROD_SYNC);
    }
}
