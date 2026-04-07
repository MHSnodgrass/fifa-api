package com.snodgrass.fifa_api.tenant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantConnectionProviderTests {
    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @InjectMocks
    private TenantConnectionProvider provider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(provider, "defaultSchema", "default_db");
    }

    @Test
    void getAnyConnection_returnsConnectionFromDataSource() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        Connection result = provider.getAnyConnection();

        assertThat(result).isEqualTo(connection);
        verify(dataSource).getConnection();
        verify(connection).setCatalog("default_db");
    }

    @Test
    void releaseAnyConnection_closesConnection() throws SQLException {
        provider.releaseAnyConnection(connection);

        verify(connection).close();
    }

    @Test
    void getConnection_withTenant_setsCatalog() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        Connection result = provider.getConnection("test_db");

        assertThat(result).isEqualTo(connection);
        verify(connection).setCatalog("test_db");
    }

    @Test
    void getConnection_whenSetCatalogFails_closesConnectionAndThrows() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(new SQLException("Catalog error")).when(connection).setCatalog(anyString());

        assertThatThrownBy(() -> provider.getConnection("invalid_db"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("Could not switch to schema: invalid_db");

        verify(connection).close();
    }

    @Test
    void releaseConnection_resetsCatalogAndCloses() throws SQLException {
        provider.releaseConnection("test_db", connection);

        verify(connection).setCatalog("default_db");
        verify(connection).close();
    }

    @Test
    void releaseConnection_whenSetCatalogFails_stillClosesConnection() throws SQLException {
        doThrow(new SQLException("Catalog error")).when(connection).setCatalog(anyString());

        provider.releaseConnection("test_db", connection);

        verify(connection).close(); // Ensure releaseAnyConnection is still called via finally
    }
}
