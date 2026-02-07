package com.cafeflow.core.exception;

import lombok.Getter;

@Getter
public class HelperException extends RuntimeException {

    private final String serviceName;
    private final String operation;

    public HelperException(String serviceName, String operation, Throwable cause) {
        super(String.format("Failed to execute %s.%s", serviceName, operation), cause);
        this.serviceName = serviceName;
        this.operation = operation;
    }

    public HelperException(String message) {
        super(message);
        this.serviceName = "unknown";
        this.operation = "unknown";
    }

    public HelperException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = "unknown";
        this.operation = "unknown";
    }
}
