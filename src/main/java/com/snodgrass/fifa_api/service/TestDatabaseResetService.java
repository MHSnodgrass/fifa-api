package com.snodgrass.fifa_api.service;

import com.snodgrass.fifa_api.exception.DatabaseResetException;
import com.snodgrass.fifa_api.model.enums.ResetMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TestDatabaseResetService {
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.tenant.default-schema}")
    private String defaultSchema;

    @Value("${app.tenant.test-schema}")
    private String testSchema;

    @Value("${app.tenant.template-schema}")
    private String templateSchema;

    public TestDatabaseResetService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String resetTestDatabase(ResetMode mode) {
        String sourceSchema = resolveSourceSchema(mode);
        log.info("Resetting test schema '{}' from source schema '{}' using mode '{}'", testSchema, sourceSchema, mode);

        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
            jdbcTemplate.execute("TRUNCATE TABLE " + testSchema + ".events");
            jdbcTemplate.execute("TRUNCATE TABLE " + testSchema + ".teams");

            jdbcTemplate.update(buildTeamCopySql(sourceSchema));
            jdbcTemplate.update(buildEventCopySql(sourceSchema));

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
            return sourceSchema;
        } catch (Exception ex) {
            throw new DatabaseResetException(
                    "Failed to reset test database '" + testSchema + "' from source '" + sourceSchema + "'",
                    ex
            );
        } finally {
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
            } catch (Exception ex) {
                log.warn("Failed to restore FOREIGN_KEY_CHECKS after reset attempt: {}", ex.getMessage());
            }
        }
    }

    private String resolveSourceSchema(ResetMode mode) {
        return switch (mode) {
            case TEMPLATE -> templateSchema;
            case PROD_SYNC -> defaultSchema;
        };
    }

    private String buildTeamCopySql(String sourceSchema) {
        return "INSERT INTO " + testSchema + ".teams (" +
                "id, country_name, country_code, flag_url, logo_url, fifa_ranking, " +
                "group_letter, manager_name, squad, matches_played, wins, draws, losses, " +
                "goals_for, goals_against, goal_difference, group_points, yellow_cards, " +
                "red_cards, eliminated, created_at, updated_at" +
                ") SELECT " +
                "id, country_name, country_code, flag_url, logo_url, fifa_ranking, " +
                "group_letter, manager_name, squad, matches_played, wins, draws, losses, " +
                "goals_for, goals_against, goal_difference, group_points, yellow_cards, " +
                "red_cards, eliminated, created_at, updated_at " +
                "FROM " + sourceSchema + ".teams";
    }

    private String buildEventCopySql(String sourceSchema) {
        return "INSERT INTO " + testSchema + ".events (" +
                "id, match_number, stage, group_letter, home_team_id, away_team_id, " +
                "home_team_placeholder, away_team_placeholder, match_date, kickoff_time, " +
                "kickoff_utc, arena_name, city, status, match_state, home_score, away_score, " +
                "winner_team_id, is_draw, has_extra_time, has_penalties, created_at, updated_at" +
                ") SELECT " +
                "id, match_number, stage, group_letter, home_team_id, away_team_id, " +
                "home_team_placeholder, away_team_placeholder, match_date, kickoff_time, " +
                "kickoff_utc, arena_name, city, status, match_state, home_score, away_score, " +
                "winner_team_id, is_draw, has_extra_time, has_penalties, created_at, updated_at " +
                "FROM " + sourceSchema + ".events";
    }
}
