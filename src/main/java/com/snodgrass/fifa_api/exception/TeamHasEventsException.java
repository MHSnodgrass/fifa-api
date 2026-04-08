package com.snodgrass.fifa_api.exception;

public class TeamHasEventsException extends RuntimeException {
    public TeamHasEventsException(Long teamId) {
        super("Cannot delete team with id: " + teamId + " because it has associated events.");
    }
}
