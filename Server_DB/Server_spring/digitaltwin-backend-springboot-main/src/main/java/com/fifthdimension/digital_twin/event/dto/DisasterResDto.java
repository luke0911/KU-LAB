package com.fifthdimension.digital_twin.event.dto;

import com.fifthdimension.digital_twin.event.domain.Disaster;
import com.fifthdimension.digital_twin.event.domain.EventStatus;
import com.fifthdimension.digital_twin.event.domain.DisasterType;
import com.fifthdimension.digital_twin.global.entity.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisasterResDto {

    private Long disasterId;
    private Integer mapId;
    private Float disasterFloor;
    private Point disasterPoint;
    private DisasterType disasterType;
    private EventStatus eventStatus;
    private String reporterAccountId;
    private String reporterName;
    private String eventDetails;

    public static DisasterResDto from(Disaster disaster) {
        return DisasterResDto.builder()
                .disasterId(disaster.getId())
                .mapId(disaster.getMap().getId())
                .disasterFloor(disaster.getFloor())
                .disasterPoint(disaster.getDisasterPoint())
                .disasterType(disaster.getDisasterType())
                .eventStatus(disaster.getEventStatus())
                .reporterAccountId(disaster.getReporter().getAccountId())
                .reporterName(disaster.getReporter().getName())
                .eventDetails(disaster.getDetails())
                .build();
    }
}
