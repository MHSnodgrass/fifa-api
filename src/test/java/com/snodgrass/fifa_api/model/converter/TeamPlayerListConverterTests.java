package com.snodgrass.fifa_api.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snodgrass.fifa_api.model.TeamPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeamPlayerListConverterTests {

    private TeamPlayerListConverter converter;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() {
        converter = new TeamPlayerListConverter();
    }

    // convertToDatabaseColumn

    @Test
    void convertToDatabaseColumn_withValidList_returnsJsonString() throws JsonProcessingException {
        List<TeamPlayer> players = List.of(
                new TeamPlayer("Neymar Jr", 10, "FW", true),
                new TeamPlayer("Alisson", 1, "GK", false)
        );

        String json = converter.convertToDatabaseColumn(players);

        assertThat(json).isNotNull();
        List<TeamPlayer> parsed = MAPPER.readValue(json, new TypeReference<>() {});
        assertThat(parsed).hasSize(2);
        assertThat(parsed.get(0).getName()).isEqualTo("Neymar Jr");
        assertThat(parsed.get(0).getNumber()).isEqualTo(10);
        assertThat(parsed.get(0).getPosition()).isEqualTo("FW");
        assertThat(parsed.get(0).getIsCaptain()).isTrue();
        assertThat(parsed.get(1).getName()).isEqualTo("Alisson");
    }

    @Test
    void convertToDatabaseColumn_withNullList_returnsNull() {
        String result = converter.convertToDatabaseColumn(null);
        assertThat(result).isNull();
    }

    @Test
    void convertToDatabaseColumn_withEmptyList_returnsNull() {
        String result = converter.convertToDatabaseColumn(Collections.emptyList());
        assertThat(result).isNull();
    }

    // convertToEntityAttribute

    @Test
    void convertToEntityAttribute_withValidJson_returnsList() {
        String json = "[{\"name\":\"Neymar Jr\",\"number\":10,\"position\":\"FW\",\"isCaptain\":true}]";

        List<TeamPlayer> result = converter.convertToEntityAttribute(json);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Neymar Jr");
        assertThat(result.get(0).getNumber()).isEqualTo(10);
        assertThat(result.get(0).getPosition()).isEqualTo("FW");
        assertThat(result.get(0).getIsCaptain()).isTrue();
    }

    @Test
    void convertToEntityAttribute_withMultiplePlayers_returnsFullList() {
        String json = "[{\"name\":\"Neymar Jr\",\"number\":10,\"position\":\"FW\",\"isCaptain\":true}," +
                "{\"name\":\"Alisson\",\"number\":1,\"position\":\"GK\",\"isCaptain\":false}]";

        List<TeamPlayer> result = converter.convertToEntityAttribute(json);

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getName()).isEqualTo("Alisson");
        assertThat(result.get(1).getIsCaptain()).isFalse();
    }

    @Test
    void convertToEntityAttribute_withNullString_returnsEmptyList() {
        List<TeamPlayer> result = converter.convertToEntityAttribute(null);
        assertThat(result).isEmpty();
    }

    @Test
    void convertToEntityAttribute_withBlankString_returnsEmptyList() {
        List<TeamPlayer> result = converter.convertToEntityAttribute("   ");
        assertThat(result).isEmpty();
    }

    @Test
    void convertToEntityAttribute_withEmptyString_returnsEmptyList() {
        List<TeamPlayer> result = converter.convertToEntityAttribute("");
        assertThat(result).isEmpty();
    }

    @Test
    void convertToEntityAttribute_withMalformedJson_returnsEmptyList() {
        List<TeamPlayer> result = converter.convertToEntityAttribute("{not valid json");
        assertThat(result).isEmpty();
    }

    // Round-trip

    @Test
    void roundTrip_serializeAndDeserialize_preservesData() {
        List<TeamPlayer> original = List.of(
                new TeamPlayer("Neymar Jr", 10, "FW", true),
                new TeamPlayer("Alisson", 1, "GK", false)
        );

        String json = converter.convertToDatabaseColumn(original);
        List<TeamPlayer> result = converter.convertToEntityAttribute(json);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo(original.get(0).getName());
        assertThat(result.get(0).getNumber()).isEqualTo(original.get(0).getNumber());
        assertThat(result.get(0).getPosition()).isEqualTo(original.get(0).getPosition());
        assertThat(result.get(0).getIsCaptain()).isEqualTo(original.get(0).getIsCaptain());
        assertThat(result.get(1).getName()).isEqualTo(original.get(1).getName());
    }
}
