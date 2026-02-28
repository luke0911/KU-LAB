package com.fifthdimension.digital_twin.area.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum EventMessageType {
    INFO("INFO"),
    WARN("WARN"),
    DANGER("DANGER");

    private final String value;

    EventMessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventMessageType fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (EventMessageType zoneType : EventMessageType.values()) {
                if (zoneType.getValue().equals(value)) {
                    return zoneType;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 Event Zone Type 입니다: " + value);
    }
}
