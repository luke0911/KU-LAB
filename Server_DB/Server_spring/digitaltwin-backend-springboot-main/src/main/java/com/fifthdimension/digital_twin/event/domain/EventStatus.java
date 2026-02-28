package com.fifthdimension.digital_twin.event.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum EventStatus {
    REPORTED("REPORTED"),
    RECEIVED("RECEIVED"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String value;

    EventStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventStatus fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (EventStatus status : EventStatus.values()) {
                if (status.getValue().equals(value)) {
                    return status;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 Event Status 입니다: " + value);
    }
}
