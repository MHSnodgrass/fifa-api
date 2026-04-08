package com.snodgrass.fifa_api.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.snodgrass.fifa_api.config.ApiHeaders;
import com.snodgrass.fifa_api.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Component("tenantInterceptor")
public class TenantInterceptor implements HandlerInterceptor {
    @Value("${app.tenant.default-schema}")
    private String defaultSchema;

    @Value("${app.tenant.test-schema}")
    private String testSchema;

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
        String tenant = request.getHeader(ApiHeaders.TEST_HEADER);
        boolean isTestContext = ApiHeaders.TEST_HEADER_VALUE.equalsIgnoreCase(tenant);

        if (isTestContext) {
            TenantContext.setCurrentTenant(testSchema);
        } else {
            TenantContext.setCurrentTenant(defaultSchema);
        }

        // CUD Http Methods are only to be used on the test database
        if (MUTATING_METHODS.contains(request.getMethod()) && !isTestContext) {
            log.warn("Blocked {} request to {} — missing or invalid {} header", request.getMethod(), request.getRequestURI(), ApiHeaders.TEST_HEADER);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse errorResponse = new ErrorResponse(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Write operations require the " + ApiHeaders.TEST_HEADER + ": " + ApiHeaders.TEST_HEADER_VALUE + " header",
                    LocalDateTime.now()
            );
            OBJECT_MAPPER.writeValue(response.getWriter(), errorResponse);

            return false;
        }

        log.debug("Setting tenant context to: {}", TenantContext.getCurrentTenant());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        TenantContext.clear();
    }
}
