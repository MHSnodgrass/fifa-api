package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.enums.Group;

import java.util.List;

public record GroupStandingsResponse(
        Group groupLetter,
        List<GroupStandingsTeamResponse> teams
) {}
