package com.fifthdimension.digital_twin.area.dto;

import com.fifthdimension.digital_twin.area.domain.EventMessageType;
import com.fifthdimension.digital_twin.area.domain.EventTrigger;
import com.fifthdimension.digital_twin.area.domain.EventTriggerType;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTriggerResDto {

    private Long triggerId;
    private String triggerName;
    private Long eventAreaId;
    private Set<UserRole> targetUserRoles;
    private EventTriggerType triggerType;
    private String eventMessage;
    private EventMessageType eventMessageType;
    private Long delay;
    private Boolean isActive;

    public static EventTriggerResDto from(EventTrigger eventTrigger) {
        return EventTriggerResDto.builder()
                .triggerId(eventTrigger.getId())
                .triggerName(eventTrigger.getTriggerName())
                .eventAreaId(eventTrigger.getId())
                .targetUserRoles(eventTrigger.getTargetUserRoles())
                .triggerType(eventTrigger.getTriggerType())
                .eventMessage(eventTrigger.getEventMessage())
                .eventMessageType(eventTrigger.getEventMessageType())
                .delay(eventTrigger.getDelay())
                .isActive(eventTrigger.getIsActive())
                .build();
    }
}
