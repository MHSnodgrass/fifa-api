package com.snodgrass.fifa_api.dto;

import com.snodgrass.fifa_api.dto.request.TeamStatsRequest;
import com.snodgrass.fifa_api.model.TeamStats;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TeamStatsRequestTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private TeamStatsRequest validStats() {
        return new TeamStatsRequest(3, 2, 1, 0, 5, 2, 3, 7, 3, 0, false);
    }

    @Test
    void validStats_hasNoViolations() {
        Set<ConstraintViolation<TeamStatsRequest>> violations = validator.validate(validStats());
        assertThat(violations).isEmpty();
    }

    @Test
    void allZeros_hasNoViolations() {
        TeamStatsRequest stats = new TeamStatsRequest(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        Set<ConstraintViolation<TeamStatsRequest>> violations = validator.validate(stats);
        assertThat(violations).isEmpty();
    }

    @Test
    void negativeGoalDifference_hasNoViolations() {
        TeamStatsRequest stats = new TeamStatsRequest(3, 0, 0, 3, 1, 5, -4, 0, 0, 0, false);
        Set<ConstraintViolation<TeamStatsRequest>> violations = validator.validate(stats);
        assertThat(violations).isEmpty();
    }

    // @Min(0) fields
    @ParameterizedTest(name = "negative {0} has violation")
    @ValueSource(strings = {"matchesPlayed", "wins", "draws", "losses", "goalsFor",
            "goalsAgainst", "groupPoints", "yellowCards", "redCards"})
    void negativeMinZeroField_hasViolation(String fieldName) {
        // Build a stats object with the target field set to -1
        TeamStatsRequest stats = new TeamStatsRequest(
                fieldName.equals("matchesPlayed") ? -1 : 0,
                fieldName.equals("wins") ? -1 : 0,
                fieldName.equals("draws") ? -1 : 0,
                fieldName.equals("losses") ? -1 : 0,
                fieldName.equals("goalsFor") ? -1 : 0,
                fieldName.equals("goalsAgainst") ? -1 : 0,
                0, // goalDifference — no @Min
                fieldName.equals("groupPoints") ? -1 : 0,
                fieldName.equals("yellowCards") ? -1 : 0,
                fieldName.equals("redCards") ? -1 : 0,
                false
        );

        Set<ConstraintViolation<TeamStatsRequest>> violations = validator.validate(stats);
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
    }

    // toEntity

    @Test
    void toEntity_mapsAllFields() {
        TeamStatsRequest request = new TeamStatsRequest(3, 2, 1, 0, 5, 2, 3, 7, 3, 1, false);

        TeamStats entity = request.toEntity();

        assertThat(entity.getMatchesPlayed()).isEqualTo(3);
        assertThat(entity.getWins()).isEqualTo(2);
        assertThat(entity.getDraws()).isEqualTo(1);
        assertThat(entity.getLosses()).isEqualTo(0);
        assertThat(entity.getGoalsFor()).isEqualTo(5);
        assertThat(entity.getGoalsAgainst()).isEqualTo(2);
        assertThat(entity.getGoalDifference()).isEqualTo(3);
        assertThat(entity.getGroupPoints()).isEqualTo(7);
        assertThat(entity.getYellowCards()).isEqualTo(3);
        assertThat(entity.getRedCards()).isEqualTo(1);
        assertThat(entity.isEliminated()).isFalse();
    }

    @Test
    void toEntity_withEliminated_mapsCorrectly() {
        TeamStatsRequest request = new TeamStatsRequest(3, 0, 0, 3, 1, 5, -4, 0, 0, 0, true);

        TeamStats entity = request.toEntity();

        assertThat(entity.isEliminated()).isTrue();
        assertThat(entity.getGoalDifference()).isEqualTo(-4);
    }
}
