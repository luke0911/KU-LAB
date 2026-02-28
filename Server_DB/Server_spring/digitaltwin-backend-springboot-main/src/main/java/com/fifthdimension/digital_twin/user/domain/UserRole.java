package com.fifthdimension.digital_twin.user.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN"),
    MASTER("MASTER");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserRole fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (UserRole role : values()) {
                if (role.getValue().equals(value)) {
                    return role;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 역할입니다: " + value);
    }
}