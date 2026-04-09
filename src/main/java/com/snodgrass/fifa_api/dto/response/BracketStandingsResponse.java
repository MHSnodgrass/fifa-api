package com.snodgrass.fifa_api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BracketStandingsResponse(
        List<BracketMatchResponse> roundOf32,
        List<BracketMatchResponse> roundOf16,
        List<BracketMatchResponse> quarterfinal,
        List<BracketMatchResponse> semifinal,
        List<BracketMatchResponse> thirdPlace,
        @JsonProperty("final") List<BracketMatchResponse> finalStage
) {}
