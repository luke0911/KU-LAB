package com.fifthdimension.digital_twin.event.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum AccidentType {
    FALL("FALL"), // 추락
    SLIP("SLIP"), // 미끄러짐
    TRIP("TRIP"), // 걸려 넘어짐
    DROP("DROP"), // 낙하
    ELECTRIC_SHOCK("ELECTRIC_SHOCK"), // 감전
    CUT("CUT"), // 베임
    PINCH("PINCH"), // 끼임/압궤
    BURN("BURN"), // 화상
    IMPACT("IMPACT"), // 충돌/충격
    INHALATION("INHALATION"), // 흡입(유해물질)
    INFECTION("INFECTION"), // 감염
    OVEREXERTION("OVEREXERTION"), // 과로/과로사
    STRANDING("STRANDING"), // 고립/고착
    VIOLENCE("VIOLENCE"), // 폭력
    OTHER_ACCIDENT("OTHER_ACCIDENT"); // 기타 사고

    private final String value;

    AccidentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccidentType fromString(String value) {
        if (value != null) {
            value = value.trim().toUpperCase();
            for (AccidentType type : AccidentType.values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 Accident Type 입니다: " + value);
    }
}
