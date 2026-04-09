package com.snodgrass.fifa_api.controller;

import com.snodgrass.fifa_api.config.ApiHeaders;
import com.snodgrass.fifa_api.dto.response.ResetDbResponse;
import com.snodgrass.fifa_api.model.enums.ResetMode;
import com.snodgrass.fifa_api.service.TestDatabaseResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "Endpoints for managing test database state")
public class TestController {
    private final TestDatabaseResetService resetService;

    @Value("${app.tenant.test-schema}")
    private String testSchema;

    @Operation(summary = "Reset test database from template or production schema",
            description = "Requires the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header to allow this write operation.",
            parameters = @Parameter(name = ApiHeaders.TEST_HEADER, in = ParameterIn.HEADER, required = true,
                    description = "Must be '" + ApiHeaders.TEST_HEADER_VALUE + "' to enable write operations on the test database"))
    @ApiResponse(responseCode = "200", description = "Test database reset completed")
    @ApiResponse(responseCode = "400", description = "Invalid reset mode")
    @ApiResponse(responseCode = "403", description = "Missing or invalid " + ApiHeaders.TEST_HEADER + " header")
    @ApiResponse(responseCode = "500", description = "Reset operation failed")
    @PostMapping("/reset-db/{mode}")
    public ResponseEntity<ResetDbResponse> resetDatabase(@PathVariable ResetMode mode) {
        log.debug("POST /api/test/reset-db/{} - Resetting test database", mode);
        String sourceSchema = resetService.resetTestDatabase(mode);
        return ResponseEntity.ok(new ResetDbResponse(
                "Test database reset successfully",
                mode,
                sourceSchema,
                testSchema
        ));
    }
}
