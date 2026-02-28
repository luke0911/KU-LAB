package com.fifthdimension.digital_twin.area.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum EventTriggerType {
    ENTRY("ENTRY"),
    EXIT("EXIT"),
    STAY("STAY");

    private final String value;

    EventTriggerType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventTriggerType fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (EventTriggerType zoneType : EventTriggerType.values()) {
                if (zoneType.getValue().equals(value)) {
                    return zoneType;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 Event Zone Type 입니다: " + value);
    }
}
