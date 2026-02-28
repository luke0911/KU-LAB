package com.fifthdimension.digital_twin.event.dto;

import com.fifthdimension.digital_twin.event.domain.Disaster;
import com.fifthdimension.digital_twin.event.domain.EventStatus;
import com.fifthdimension.digital_twin.event.domain.DisasterType;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DisasterCreateReqDto {

    private Integer mapId;
    private Float disasterFloor;
    private Point disasterPoint;
    private DisasterType disasterType;
    private String eventDetails;

    public Disaster toEntity(Map map, User reporter){
        return Disaster.builder()
                .map(map)
                .floor(this.disasterFloor)
                .disasterPoint(this.disasterPoint)
                .disasterType(this.disasterType)
                .eventStatus(EventStatus.REPORTED) // 최초 생성시 "신고" 상태
                .reporter(reporter)
                .details(this.eventDetails)
                .build();
    }
}
