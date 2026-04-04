package com.snodgrass.fifa_api.dto;

public record PlayerResponse (
    String name,
    Integer number,
    String position,
    Boolean isCaptain
) {}
