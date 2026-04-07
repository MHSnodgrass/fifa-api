package com.snodgrass.fifa_api.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TenantIdentifierResolverTests {
    private TenantIdentifierResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new TenantIdentifierResolver();
        ReflectionTestUtils.setField(resolver, "defaultSchema", "default_db");
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void resolveCurrentTenantIdentifier_withContextSet_returnsTenant() {
        TenantContext.setCurrentTenant("test_db");

        String tenant = resolver.resolveCurrentTenantIdentifier();

        assertThat(tenant).isEqualTo("test_db");
    }

    @Test
    void resolveCurrentTenantIdentifier_withNoContext_returnsDefaultSchema() {
        String tenant = resolver.resolveCurrentTenantIdentifier();

        assertThat(tenant).isEqualTo("default_db");
    }

    @Test
    void validateExistingCurrentSessions_returnsTrue() {
        assertThat(resolver.validateExistingCurrentSessions()).isTrue();
    }
}
