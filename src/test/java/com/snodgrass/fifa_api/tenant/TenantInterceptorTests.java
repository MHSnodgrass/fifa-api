package com.snodgrass.fifa_api.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantInterceptorTests {

    @InjectMocks
    private TenantInterceptor tenantInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tenantInterceptor, "defaultSchema", "default_db");
        ReflectionTestUtils.setField(tenantInterceptor, "testSchema", "test_db");
        ReflectionTestUtils.setField(tenantInterceptor, "httpTestHeader", "X-DB-STATE");
        ReflectionTestUtils.setField(tenantInterceptor, "httpTestHeaderValue", "MODIFIED");
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void preHandle_withValidHeader_setsTestSchema() {
        when(request.getHeader("X-DB-STATE")).thenReturn("MODIFIED");

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("test_db");
    }

    @Test
    void preHandle_withInvalidHeader_setsDefaultSchema() {
        when(request.getHeader("X-DB-STATE")).thenReturn("OTHER");

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("default_db");
    }

    @Test
    void preHandle_withoutHeader_setsDefaultSchema() {
        when(request.getHeader("X-DB-STATE")).thenReturn(null);

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("default_db");
    }

    @Test
    void preHandle_withDifferentCaseHeaderValue_setsTestSchema() {
        when(request.getHeader("X-DB-STATE")).thenReturn("modified");

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("test_db");
    }

    @Test
    void afterCompletion_clearsContext() {
        TenantContext.setCurrentTenant("some_tenant");

        tenantInterceptor.afterCompletion(request, response, handler, null);

        assertThat(TenantContext.getCurrentTenant()).isNull();
    }
}
