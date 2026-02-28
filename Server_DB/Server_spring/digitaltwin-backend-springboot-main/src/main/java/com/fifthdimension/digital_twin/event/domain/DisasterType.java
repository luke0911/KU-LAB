package com.fifthdimension.digital_twin.event.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum DisasterType {
    FIRE("FIRE"), // 화재
    GAS_LEAK("GAS_LEAK"), // 가스 누출
    EXPLOSION("EXPLOSION"), // 폭발
    COLLAPSE("COLLAPSE"), // 붕괴
    FLOOD("FLOOD"), // 홍수
    EARTHQUAKE("EARTHQUAKE"), // 지진
    POWER_OUTAGE("POWER_OUTAGE"), // 정전
    HEATWAVE("HEATWAVE"), // 폭염
    COLDWAVE("COLDWAVE"), // 한파
    TYPHOON("TYPHOON"), // 태풍
    HEAVY_SNOW("HEAVY_SNOW"), // 폭설
    CHEMICAL_LEAK("CHEMICAL_LEAK"), // 화학물질 누출, 오염사고
    OTHER_DISASTER("OTHER_DISASTER"); // 기타 재해

    private final String value;

    DisasterType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DisasterType fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (DisasterType type : DisasterType.values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 Disaster Type 입니다: " + value);
    }
}
