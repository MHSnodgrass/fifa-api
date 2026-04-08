package com.snodgrass.fifa_api.dto;

import com.snodgrass.fifa_api.dto.request.PlayerRequest;
import com.snodgrass.fifa_api.dto.request.TeamRequest;
import com.snodgrass.fifa_api.dto.request.TeamStatsRequest;

import com.snodgrass.fifa_api.model.enums.Group;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TeamRequestTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private TeamRequest validRequest() {
        return new TeamRequest("Brazil", "BRA", Group.A,
                "/flags/bra.png", "/logos/bra.png", 1, "Dorival Júnior",
                null, null);
    }

    @Test
    void validRequest_hasNoViolations() {
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(validRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void validRequest_withAllOptionalsNull_hasNoViolations() {
        TeamRequest request = new TeamRequest("Brazil", "BRA", Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    // countryName
    @Test
    void nullCountryName_hasViolation() {
        TeamRequest request = new TeamRequest(null, "BRA", Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("countryName"));
    }

    @Test
    void blankCountryName_hasViolation() {
        TeamRequest request = new TeamRequest("", "BRA", Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("countryName"));
    }

    @Test
    void countryNameTooLong_hasViolation() {
        TeamRequest request = new TeamRequest("A".repeat(101), "BRA", Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("countryName"));
    }

    // countryCode
    @Test
    void nullCountryCode_hasViolation() {
        TeamRequest request = new TeamRequest("Brazil", null, Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("countryCode"));
    }

    @Test
    void countryCodeTooShort_hasViolation() {
        TeamRequest request = new TeamRequest("Brazil", "BR", Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("countryCode"));
    }

    @Test
    void countryCodeTooLong_hasViolation() {
        TeamRequest request = new TeamRequest("Brazil", "BRAZ", Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("countryCode"));
    }

    // groupLetter
    @Test
    void nullGroupLetter_hasViolation() {
        TeamRequest request = new TeamRequest("Brazil", "BRA", null,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("groupLetter"));
    }

    // fifaRanking
    @Test
    void fifaRankingZero_hasViolation() {
        TeamRequest request = new TeamRequest("Brazil", "BRA", Group.A,
                null, null, 0, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fifaRanking"));
    }

    @Test
    void fifaRankingNull_hasNoViolations() {
        TeamRequest request = new TeamRequest("Brazil", "BRA", Group.A,
                null, null, null, null, null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    // managerName
    @Test
    void managerNameTooLong_hasViolation() {
        TeamRequest request = new TeamRequest("Brazil", "BRA", Group.A,
                null, null, null, "A".repeat(101), null, null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("managerName"));
    }

    // Cascading @Valid on squad
    @Test
    void invalidPlayerInSquad_hasViolation() {
        PlayerRequest invalidPlayer = new PlayerRequest("", 10, "FW", true);
        TeamRequest request = new TeamRequest("Brazil", "BRA", Group.A,
                null, null, null, null, List.of(invalidPlayer), null);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("squad"));
    }

    // Cascading @Valid on stats
    @Test
    void invalidStats_hasViolation() {
        TeamStatsRequest invalidStats = new TeamStatsRequest(-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        TeamRequest request = new TeamRequest("Brazil", "BRA", Group.A,
                null, null, null, null, null, invalidStats);
        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("stats"));
    }
}
