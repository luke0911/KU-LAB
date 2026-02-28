package com.fifthdimension.digital_twin.area.dto;

import com.fifthdimension.digital_twin.area.domain.EventMessageType;
import com.fifthdimension.digital_twin.area.domain.EventTriggerType;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventTriggerUpdateReqDto {

    private Long triggerId;
    @Size(min = 1, max = 100, message = "Trigger 이름은 100자 이하로 입력해야 합니다.")
    private String triggerName;
    @NotEmpty(message = "Target UserRole이 최소 하나 이상 포함되어야 합니다.")
    private Set<UserRole> targetUserRoles;
    private EventTriggerType triggerType;
    private String eventMessage;
    private EventMessageType eventMessageType;
    private Long delay;
    private Boolean isActive;

}
