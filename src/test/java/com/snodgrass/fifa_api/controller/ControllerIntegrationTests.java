package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.config.ApiHeaders;
import com.snodgrass.fifa_api.dto.response.EventResponse;
import com.snodgrass.fifa_api.dto.response.TeamDetailResponse;
import com.snodgrass.fifa_api.dto.response.TeamResponse;
import com.snodgrass.fifa_api.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerIntegrationTests {
    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    // Team
    @Test
    void getAllTeams_returns200WithNonEmptyList() {
        ResponseEntity<List<TeamResponse>> response = restClient.get()
                .uri("/api/teams")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getTeamById_returns404_whenNotFound() {
        ResponseEntity<ErrorResponse> response = restClient.get()
                .uri("/api/teams/999999")
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {})
                .toEntity(ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("999999");
    }

    // Event
    @Test
    void getAllEvents_returns200WithNonEmptyList() {
        ResponseEntity<List<EventResponse>> response = restClient.get()
                .uri("/api/events")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getEventById_returns404_whenNotFound() {
        ResponseEntity<ErrorResponse> response = restClient.get()
                .uri("/api/events/999999")
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {})
                .toEntity(ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("999999");
    }

    // Team CUD

    private String teamJson(String countryName, String countryCode) {
        return """
                {
                    "countryName": "%s",
                    "countryCode": "%s",
                    "groupLetter": "A",
                    "fifaRanking": 50,
                    "squad": [{"name": "Test Player", "number": 10, "position": "FW", "isCaptain": true}],
                    "stats": {"matchesPlayed": 0, "wins": 0, "draws": 0, "losses": 0, "goalsFor": 0, "goalsAgainst": 0, "goalDifference": 0, "groupPoints": 0, "yellowCards": 0, "redCards": 0, "eliminated": false}
                }
                """.formatted(countryName, countryCode);
    }

    private String randomCode() {
        return UUID.randomUUID().toString().substring(0, 3).toUpperCase();
    }

    @Test
    void createTeam_returns201() {
        String code = randomCode();
        String name = "Create Test " + code;
        String json = teamJson(name, code);

        ResponseEntity<TeamDetailResponse> response = restClient.post()
                .uri("/api/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(TeamDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().countryName()).isEqualTo(name);
        assertThat(response.getBody().squad()).hasSize(1);
    }

    @Test
    void updateTeam_returns200() {
        // First create a team with unique data
        String createCode = randomCode();
        String createName = "Update Pre " + createCode;
        ResponseEntity<TeamDetailResponse> createResponse = restClient.post()
                .uri("/api/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson(createName, createCode))
                .retrieve()
                .toEntity(TeamDetailResponse.class);

        Long teamId = createResponse.getBody().id();

        String updateCode = randomCode();
        String updateName = "Update Post " + updateCode;
        ResponseEntity<TeamDetailResponse> response = restClient.put()
                .uri("/api/teams/" + teamId)
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson(updateName, updateCode))
                .retrieve()
                .toEntity(TeamDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().countryName()).isEqualTo(updateName);
        assertThat(response.getBody().countryCode()).isEqualTo(updateCode);
    }

    @Test
    void deleteTeam_returns204() {
        // First create a team with unique data to delete
        String delCode = randomCode();
        ResponseEntity<TeamDetailResponse> createResponse = restClient.post()
                .uri("/api/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson("Delete Test " + delCode, delCode))
                .retrieve()
                .toEntity(TeamDetailResponse.class);

        Long teamId = createResponse.getBody().id();

        ResponseEntity<Void> response = restClient.delete()
                .uri("/api/teams/" + teamId)
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void createTeam_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.post()
                .uri("/api/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson("Forbidden Country", randomCode()))
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updateTeam_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.put()
                .uri("/api/teams/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson("Forbidden Update", randomCode()))
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteTeam_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.delete()
                .uri("/api/teams/1")
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
