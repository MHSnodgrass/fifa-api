package com.snodgrass.fifa_api.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component("tenantInterceptor")
public class TenantInterceptor implements HandlerInterceptor {
    @Value("${app.tenant.default-schema}")
    private String defaultSchema;

    @Value("${app.tenant.test-schema}")
    private String testSchema;

    @Value("${app.tenant.http-test-header}")
    private String httpTestHeader;

    @Value("${app.tenant.http-test-header-value}")
    private String httpTestHeaderValue;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String tenant = request.getHeader(httpTestHeader);

        if (httpTestHeaderValue.equalsIgnoreCase(tenant)) {
            TenantContext.setCurrentTenant(testSchema);
        } else {
            TenantContext.setCurrentTenant(defaultSchema);
        }

        log.debug("Setting tenant context to: {}", TenantContext.getCurrentTenant());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        TenantContext.clear();
    }
}
