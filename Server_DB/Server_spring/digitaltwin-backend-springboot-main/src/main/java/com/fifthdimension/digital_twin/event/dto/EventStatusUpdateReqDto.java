package com.fifthdimension.digital_twin.event.dto;

import com.fifthdimension.digital_twin.event.domain.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusUpdateReqDto {

    private EventStatus eventStatus;
}
