package com.snodgrass.fifa_api.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class TeamStats {
    private int matchesPlayed;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private int groupPoints;
    private int yellowCards;
    private int redCards;
    private boolean eliminated;
}
