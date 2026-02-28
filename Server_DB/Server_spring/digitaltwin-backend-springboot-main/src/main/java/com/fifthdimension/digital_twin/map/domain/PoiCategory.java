package com.fifthdimension.digital_twin.map.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum PoiCategory {
    FOOD("FOOD"),
    CAFE("CAFE"),
    CLOTHING("CLOTHING"),
    BEAUTY("BEAUTY"),
    SPORTS("SPORTS"),
    ENTERTAINMENT("ENTERTAINMENT"),
    ETC("ETC");

    private final String value;

    PoiCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PoiCategory fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (PoiCategory category : PoiCategory.values()) {
                if (category.getValue().equals(value)) {
                    return category;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 POI 카테고리입니다: " + value);
    }
}