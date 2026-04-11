package com.snodgrass.fifa_api.dto.request;

import com.snodgrass.fifa_api.model.enums.SimulationCompletionStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import jakarta.validation.constraints.NotNull;

public record SimulationRequest(
        @NotNull
        Stage targetStage,

        @NotNull
        SimulationCompletionStatus completionStatus,

        Long seed
) {}
