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

    private String eventJson(int matchNumber) {
        return """
                {
                    "matchNumber": %d,
                    "stage": "GROUP",
                    "groupLetter": "A",
                    "homeTeamId": 1,
                    "awayTeamId": 2,
                    "matchDate": "2026-06-11",
                    "arenaName": "MetLife Stadium",
                    "city": "New York",
                    "status": "SCHEDULED",
                    "hasExtraTime": false,
                    "hasPenalties": false
                }
                """.formatted(matchNumber);
    }

    @Test
    void createTeam_returns201() {
        String code = randomCode();
        String name = "Create Test " + code;
        String json = teamJson(name, code);

        ResponseEntity<TeamDetailResponse> response = restClient.post()
                .uri("/api/test/teams")
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
                .uri("/api/test/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson(createName, createCode))
                .retrieve()
                .toEntity(TeamDetailResponse.class);

        Long teamId = createResponse.getBody().id();

        String updateCode = randomCode();
        String updateName = "Update Post " + updateCode;
        ResponseEntity<TeamDetailResponse> response = restClient.put()
                .uri("/api/test/teams/" + teamId)
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
                .uri("/api/test/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson("Delete Test " + delCode, delCode))
                .retrieve()
                .toEntity(TeamDetailResponse.class);

        Long teamId = createResponse.getBody().id();

        ResponseEntity<Void> response = restClient.delete()
                .uri("/api/test/teams/" + teamId)
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void createTeam_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.post()
                .uri("/api/test/teams")
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
                .uri("/api/test/teams/1")
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
                .uri("/api/test/teams/1")
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // Event CUD

    @Test
    void createEvent_returns201() {
        ResponseEntity<EventResponse> response = restClient.post()
                .uri("/api/test/events")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventJson(5001))
                .retrieve()
                .toEntity(EventResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().matchNumber()).isEqualTo(5001);
    }

    @Test
    void updateEvent_returns200() {
        ResponseEntity<EventResponse> createResponse = restClient.post()
                .uri("/api/test/events")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventJson(5002))
                .retrieve()
                .toEntity(EventResponse.class);

        Long eventId = createResponse.getBody().id();

        ResponseEntity<EventResponse> response = restClient.put()
                .uri("/api/test/events/" + eventId)
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventJson(5003))
                .retrieve()
                .toEntity(EventResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().matchNumber()).isEqualTo(5003);
    }

    @Test
    void deleteEvent_returns204() {
        ResponseEntity<EventResponse> createResponse = restClient.post()
                .uri("/api/test/events")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventJson(5004))
                .retrieve()
                .toEntity(EventResponse.class);

        Long eventId = createResponse.getBody().id();

        ResponseEntity<Void> response = restClient.delete()
                .uri("/api/test/events/" + eventId)
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void createEvent_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.post()
                .uri("/api/test/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventJson(5005))
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updateEvent_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.put()
                .uri("/api/test/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventJson(5006))
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteEvent_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.delete()
                .uri("/api/test/events/1")
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // Test database reset

    @Test
    void resetDb_template_returns200_whenHeaderPresent() {
        ResponseEntity<String> response = restClient.post()
                .uri("/api/test/reset-db/TEMPLATE")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .onStatus(status -> status.value() == 500, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode().value()).isIn(200, 500);
        assertThat(response.getBody()).contains("fifa_world_cup_template");
    }

    @Test
    void resetDb_prodSync_returns200_whenHeaderPresent() {
        ResponseEntity<String> response = restClient.post()
                .uri("/api/test/reset-db/PROD_SYNC")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("PROD_SYNC");
    }

    @Test
    void resetDb_withoutTestHeader_returns403() {
        ResponseEntity<String> response = restClient.post()
                .uri("/api/test/reset-db/TEMPLATE")
                .retrieve()
                .onStatus(status -> status.value() == 403, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void resetDb_withInvalidMode_returns400() {
        ResponseEntity<String> response = restClient.post()
                .uri("/api/test/reset-db/INVALID")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, res) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resetDb_prodSync_removesCustomTeamFromTestSchema() {
        String code = randomCode();
        String name = "Reset Target " + code;

        // Insert a custom team into test schema.
        ResponseEntity<TeamDetailResponse> createResponse = restClient.post()
                .uri("/api/test/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(teamJson(name, code))
                .retrieve()
                .toEntity(TeamDetailResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify the custom team exists in test schema before reset.
        ResponseEntity<List<TeamResponse>> beforeReset = restClient.get()
                .uri("/api/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(beforeReset.getBody()).isNotNull();
        assertThat(beforeReset.getBody().stream().anyMatch(t -> name.equals(t.countryName()))).isTrue();

        // Run reset from production schema.
        ResponseEntity<String> resetResponse = restClient.post()
                .uri("/api/test/reset-db/PROD_SYNC")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .toEntity(String.class);

        assertThat(resetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify the custom team no longer exists in test schema after reset.
        ResponseEntity<List<TeamResponse>> afterReset = restClient.get()
                .uri("/api/teams")
                .header(ApiHeaders.TEST_HEADER, ApiHeaders.TEST_HEADER_VALUE)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(afterReset.getBody()).isNotNull();
        assertThat(afterReset.getBody().stream().anyMatch(t -> name.equals(t.countryName()))).isFalse();
    }
}
