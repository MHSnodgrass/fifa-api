package com.snodgrass.fifa_api.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTests {
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void setCurrentTenant_setsTenant() {
        TenantContext.setCurrentTenant("my_tenant");

        assertThat(TenantContext.getCurrentTenant()).isEqualTo("my_tenant");
    }

    @Test
    void clear_removesTenant() {
        TenantContext.setCurrentTenant("my_tenant");
        TenantContext.clear();

        assertThat(TenantContext.getCurrentTenant()).isNull();
    }
    
    @Test
    void getCurrentTenant_returnsNullInitially() {
        assertThat(TenantContext.getCurrentTenant()).isNull();
    }
}
