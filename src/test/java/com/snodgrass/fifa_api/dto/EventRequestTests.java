package com.snodgrass.fifa_api.dto;

import com.snodgrass.fifa_api.dto.request.EventRequest;
import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EventRequestTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private EventRequest validRequest() {
        return new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), LocalTime.of(18, 0), LocalDateTime.of(2026, 6, 11, 22, 0),
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
    }

    @Test
    void validRequest_hasNoViolations() {
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(validRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void validRequest_withNullOptionals_hasNoViolations() {
        EventRequest request = new EventRequest(1, Stage.QUARTERFINAL, null,
                null, null, "Winner Group A", "Winner Group B",
                LocalDate.of(2026, 7, 4), null, null,
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    // matchNumber
    @Test
    void nullMatchNumber_hasViolation() {
        EventRequest request = new EventRequest(null, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("matchNumber"));
    }

    // stage
    @Test
    void nullStage_hasViolation() {
        EventRequest request = new EventRequest(1, null, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("stage"));
    }

    // matchDate
    @Test
    void nullMatchDate_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                null, null, null,
                "MetLife Stadium", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("matchDate"));
    }

    // arenaName
    @Test
    void nullArenaName_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                null, "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("arenaName"));
    }

    @Test
    void blankArenaName_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "", "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("arenaName"));
    }

    @Test
    void arenaNameTooLong_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "A".repeat(101), "New York",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("arenaName"));
    }

    // city
    @Test
    void nullCity_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", null,
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("city"));
    }

    @Test
    void blankCity_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", "",
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("city"));
    }


    @Test
    void cityTooLong_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", "A".repeat(101),
                MatchStatus.SCHEDULED, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("city"));
    }

    // status
    @Test
    void nullStatus_hasViolation() {
        EventRequest request = new EventRequest(1, Stage.GROUP, Group.A,
                1L, 2L, null, null,
                LocalDate.of(2026, 6, 11), null, null,
                "MetLife Stadium", "New York",
                null, null, null, null, null, false, false);
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("status"));
    }

    // toEntity

    @Test
    void toEntity_mapsAllScalarFields() {
        EventRequest request = validRequest();
        Event entity = request.toEntity();

        assertThat(entity.getMatchNumber()).isEqualTo(1);
        assertThat(entity.getStage()).isEqualTo(Stage.GROUP);
        assertThat(entity.getGroupLetter()).isEqualTo(Group.A);
        assertThat(entity.getMatchDate()).isEqualTo(LocalDate.of(2026, 6, 11));
        assertThat(entity.getKickoffTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(entity.getKickoffUtc()).isEqualTo(LocalDateTime.of(2026, 6, 11, 22, 0));
        assertThat(entity.getArenaName()).isEqualTo("MetLife Stadium");
        assertThat(entity.getCity()).isEqualTo("New York");
        assertThat(entity.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(entity.getIsDraw()).isNull();
        assertThat(entity.isHasExtraTime()).isFalse();
        assertThat(entity.isHasPenalties()).isFalse();
    }

    @Test
    void toEntity_doesNotSetTeamReferences() {
        EventRequest request = validRequest();
        Event entity = request.toEntity();

        assertThat(entity.getHomeTeam()).isNull();
        assertThat(entity.getAwayTeam()).isNull();
        assertThat(entity.getWinnerTeam()).isNull();
    }

    @Test
    void toEntity_setsTimestamps() {
        EventRequest request = validRequest();
        Event entity = request.toEntity();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}