package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.enums.SimulationCompletionStatus;
import com.snodgrass.fifa_api.model.enums.Stage;

import java.util.List;

public record SimulationResponse(
        Stage targetStage,
        SimulationCompletionStatus completionStatus,
        int simulatedMatchCount,
        List<Stage> stagesTouched,
        long seedUsed
) { }
