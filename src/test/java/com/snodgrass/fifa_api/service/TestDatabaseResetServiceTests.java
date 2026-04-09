package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.exception.DatabaseResetException;
import com.snodgrass.fifa_api.model.enums.ResetMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestDatabaseResetServiceTests {
    @Mock
    private JdbcTemplate jdbcTemplate;

    private TestDatabaseResetService service;

    @BeforeEach
    void setUp() {
        service = new TestDatabaseResetService(jdbcTemplate);
        ReflectionTestUtils.setField(service, "defaultSchema", "fifa_world_cup");
        ReflectionTestUtils.setField(service, "testSchema", "fifa_world_cup_test");
        ReflectionTestUtils.setField(service, "templateSchema", "fifa_world_cup_template");
    }

    @Test
    void resetTestDatabase_template_executesExpectedSqlOrder() {
        String source = service.resetTestDatabase(ResetMode.TEMPLATE);

        assertThat(source).isEqualTo("fifa_world_cup_template");
        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("SET FOREIGN_KEY_CHECKS=0");
        inOrder.verify(jdbcTemplate).execute("TRUNCATE TABLE fifa_world_cup_test.events");
        inOrder.verify(jdbcTemplate).execute("TRUNCATE TABLE fifa_world_cup_test.teams");
        inOrder.verify(jdbcTemplate).update(contains("FROM fifa_world_cup_template.teams"));
        inOrder.verify(jdbcTemplate).update(contains("FROM fifa_world_cup_template.events"));
        inOrder.verify(jdbcTemplate, atLeastOnce()).execute("SET FOREIGN_KEY_CHECKS=1");
    }

    @Test
    void resetTestDatabase_prodSync_executesExpectedSqlOrder() {
        String source = service.resetTestDatabase(ResetMode.PROD_SYNC);

        assertThat(source).isEqualTo("fifa_world_cup");
        verify(jdbcTemplate).update(contains("FROM fifa_world_cup.teams"));
        verify(jdbcTemplate).update(contains("FROM fifa_world_cup.events"));
    }

    @Test
    void resetTestDatabase_whenSqlFails_throwsDatabaseResetExceptionAndRestoresFkChecks() {
        doThrow(new RuntimeException("boom"))
                .when(jdbcTemplate).update(contains("FROM fifa_world_cup_template.teams"));

        assertThatThrownBy(() -> service.resetTestDatabase(ResetMode.TEMPLATE))
                .isInstanceOf(DatabaseResetException.class)
                .hasMessageContaining("fifa_world_cup_test")
                .hasMessageContaining("fifa_world_cup_template");

        verify(jdbcTemplate, atLeastOnce()).execute("SET FOREIGN_KEY_CHECKS=1");
    }
}
