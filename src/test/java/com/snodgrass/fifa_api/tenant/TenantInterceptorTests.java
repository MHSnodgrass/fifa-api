package com.snodgrass.fifa_api.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // --- Existing tenant-routing tests (now explicit about HTTP method) ---

    @Test
    void preHandle_withValidHeader_setsTestSchema() throws Exception {
        when(request.getHeader("X-DB-STATE")).thenReturn("MODIFIED");
        when(request.getMethod()).thenReturn("GET");

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("test_db");
    }

    @Test
    void preHandle_withInvalidHeader_setsDefaultSchema() throws Exception {
        when(request.getHeader("X-DB-STATE")).thenReturn("OTHER");
        when(request.getMethod()).thenReturn("GET");

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("default_db");
    }

    @Test
    void preHandle_withoutHeader_setsDefaultSchema() throws Exception {
        when(request.getHeader("X-DB-STATE")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("default_db");
    }

    @Test
    void preHandle_withDifferentCaseHeaderValue_setsTestSchema() throws Exception {
        when(request.getHeader("X-DB-STATE")).thenReturn("modified");
        when(request.getMethod()).thenReturn("GET");

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

    // --- Mutating-method blocking tests ---

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void preHandle_mutatingMethodWithoutTestHeader_returns403(String method) throws Exception {
        when(request.getHeader("X-DB-STATE")).thenReturn(null);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn("/api/teams");

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isFalse();
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");

        String body = stringWriter.toString();
        assertThat(body).contains("\"status\":403");
        assertThat(body).contains("Write operations require the X-DB-STATE: MODIFIED header");
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void preHandle_mutatingMethodWithTestHeader_allowsRequest(String method) throws Exception {
        when(request.getHeader("X-DB-STATE")).thenReturn("MODIFIED");
        when(request.getMethod()).thenReturn(method);

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("test_db");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void preHandle_getWithoutTestHeader_allowsRequest() throws Exception {
        when(request.getHeader("X-DB-STATE")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");

        boolean result = tenantInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("default_db");
        verify(response, never()).setStatus(anyInt());
    }
}
