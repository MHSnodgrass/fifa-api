package com.snodgrass.fifa_api.exception;

public class DatabaseResetException extends RuntimeException {
    public DatabaseResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
