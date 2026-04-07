package com.snodgrass.fifa_api.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component(value = "tenantIdentifierResolver")
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {
    @Value("${app.tenant.default-schema}")
    private String defaultSchema;

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();
        return tenant != null ? tenant : defaultSchema;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
