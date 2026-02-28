package com.fifthdimension.digital_twin.area.dto;

import com.fifthdimension.digital_twin.area.domain.EventArea;
import com.fifthdimension.digital_twin.area.domain.EventAreaType;
import com.fifthdimension.digital_twin.global.entity.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAreaResDto {

    private Long eventAreaId;
    private String eventAreaName;
    private Integer mapId;
    private Float eventAreaFloor;
    private List<Point> eventAreaPoints;
    private String eventAreaDescription;
    private EventAreaType eventAreaType;
    private List<EventTriggerResDto> eventTriggers;

    public static EventAreaResDto from(EventArea eventArea) {
        return EventAreaResDto.builder()
                .eventAreaId(eventArea.getId())
                .eventAreaName(eventArea.getName())
                .mapId(eventArea.getMap().getId())
                .eventAreaFloor(eventArea.getFloor())
                .eventAreaPoints(eventArea.getPoints())
                .eventAreaDescription(eventArea.getDescription())
                .eventAreaType(eventArea.getEventAreaType())
                .eventTriggers(
                        eventArea.getEventTriggers() == null ? null :
                                eventArea.getEventTriggers().stream()
                                        .map(EventTriggerResDto::from)
                                        .toList()
                )
                .build();
    }
}
