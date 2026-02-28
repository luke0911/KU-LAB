package com.fifthdimension.digital_twin.area.dto;

import com.fifthdimension.digital_twin.area.domain.*;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventTriggerCreateReqDto {

    @Size(min = 1, max = 100, message = "Trigger 이름은 100자 이하로 입력해야 합니다.")
    private String triggerName;
    private Long eventAreaId;
    @NotEmpty(message = "Target UserRole이 최소 하나 이상 포함되어야 합니다.")
    private Set<UserRole> targetUserRoles;
    private EventTriggerType triggerType;
    private String eventMessage;
    private EventMessageType eventMessageType;
    private Long delay;
    private Boolean isActive;

    public EventTrigger toEntity(EventArea eventArea) {
        return EventTrigger.builder()
                .triggerName(triggerName)
                .eventArea(eventArea)
                .targetUserRoles(targetUserRoles)
                .triggerType(triggerType)
                .eventMessage(eventMessage)
                .eventMessageType(eventMessageType)
                .delay(delay)
                .isActive(isActive)
                .build();
    }
}
