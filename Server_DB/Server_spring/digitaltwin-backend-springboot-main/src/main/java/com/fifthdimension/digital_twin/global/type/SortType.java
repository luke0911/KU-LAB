package com.fifthdimension.digital_twin.global.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum SortType {
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String value;

    SortType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SortType fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (SortType sortType : SortType.values()) {
                if (sortType.name().equals(value)) {
                    return sortType;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 Sort Type 입니다: " + value);
    }
}
