package com.fifthdimension.digital_twin.event.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum DevicePlatform {
    WEB("WEB"),
    ANDROID("ANDROID"),
    IOS("IOS");

    private final String value;

    DevicePlatform(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DevicePlatform fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (DevicePlatform platform : DevicePlatform.values()) {
                if (platform.getValue().equals(value)) {
                    return platform;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 Device Platform 입니다: " + value);
    }
}
