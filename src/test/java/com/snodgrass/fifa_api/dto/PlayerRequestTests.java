package com.snodgrass.fifa_api.dto;

import com.snodgrass.fifa_api.dto.request.PlayerRequest;
import com.snodgrass.fifa_api.model.TeamPlayer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerRequestTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private PlayerRequest validPlayer() {
        return new PlayerRequest("Neymar Jr", 10, "FW", true);
    }

    @Test
    void validPlayerRequest_hasNoViolations() {
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(validPlayer());
        assertThat(violations).isEmpty();
    }

    @Test
    void validPlayerRequest_withNullIsCaptain_hasNoViolations() {
        PlayerRequest player = new PlayerRequest("Neymar Jr", 10, "FW", null);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).isEmpty();
    }

    // name
    @Test
    void nullName_hasViolation() {
        PlayerRequest player = new PlayerRequest(null, 10, "FW", true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void blankName_hasViolation() {
        PlayerRequest player = new PlayerRequest("", 10, "FW", true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void whitespaceName_hasViolation() {
        PlayerRequest player = new PlayerRequest("   ", 10, "FW", true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    // number
    @Test
    void nullNumber_hasViolation() {
        PlayerRequest player = new PlayerRequest("Neymar Jr", null, "FW", true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("number"));
    }

    @Test
    void zeroNumber_hasViolation() {
        PlayerRequest player = new PlayerRequest("Neymar Jr", 0, "FW", true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("number"));
    }

    @Test
    void negativeNumber_hasViolation() {
        PlayerRequest player = new PlayerRequest("Neymar Jr", -1, "FW", true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("number"));
    }

    // position
    @Test
    void nullPosition_hasViolation() {
        PlayerRequest player = new PlayerRequest("Neymar Jr", 10, null, true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("position"));
    }

    @Test
    void blankPosition_hasViolation() {
        PlayerRequest player = new PlayerRequest("Neymar Jr", 10, "", true);
        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(player);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("position"));
    }

    // toEntity

    @Test
    void toEntity_mapsAllFields() {
        PlayerRequest request = new PlayerRequest("Neymar Jr", 10, "FW", true);

        TeamPlayer entity = request.toEntity();

        assertThat(entity.getName()).isEqualTo("Neymar Jr");
        assertThat(entity.getNumber()).isEqualTo(10);
        assertThat(entity.getPosition()).isEqualTo("FW");
        assertThat(entity.getIsCaptain()).isTrue();
    }

    @Test
    void toEntity_withNullIsCaptain_mapsNullCaptain() {
        PlayerRequest request = new PlayerRequest("Alisson", 1, "GK", null);

        TeamPlayer entity = request.toEntity();

        assertThat(entity.getName()).isEqualTo("Alisson");
        assertThat(entity.getIsCaptain()).isNull();
    }
}
