package com.snodgrass.fifa_api.dto.response;

import com.snodgrass.fifa_api.model.enums.ResetMode;

public record ResetDbResponse(
        String message,
        ResetMode mode,
        String sourceSchema,
        String targetSchema
) {}
