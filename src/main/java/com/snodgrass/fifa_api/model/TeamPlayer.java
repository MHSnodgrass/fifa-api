package com.snodgrass.fifa_api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamPlayer {
    private String name;
    private Integer number;
    private String position;
    private Boolean isCaptain;
}
