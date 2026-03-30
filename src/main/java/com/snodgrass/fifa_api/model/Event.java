package com.snodgrass.fifa_api.model;

import com.snodgrass.fifa_api.model.enums.Group;
import com.snodgrass.fifa_api.model.enums.MatchStatus;
import com.snodgrass.fifa_api.model.enums.Stage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_number", nullable = false)
    private Integer matchNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    private Group groupLetter;

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private String homeTeamPlaceholder;
    private String awayTeamPlaceholder;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    private LocalTime kickoffTime;
    private LocalDateTime kickoffUtc;

    @Column(name = "arena_name", nullable = false)
    private String arenaName;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(name = "match_state", columnDefinition = "JSON")
    private String matchState;

    private Integer homeScore;
    private Integer awayScore;

    @ManyToOne
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    private Boolean isDraw;
    private boolean hasExtraTime;
    private boolean hasPenalties;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}