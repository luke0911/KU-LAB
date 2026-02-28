package com.fifthdimension.digital_twin.event.dto;

import com.fifthdimension.digital_twin.event.domain.Accident;
import com.fifthdimension.digital_twin.event.domain.AccidentType;
import com.fifthdimension.digital_twin.event.domain.EventStatus;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccidentCreateReqDto {

    private Integer mapId;
    private Float accidentFloor;
    private Point accidentPoint;
    private AccidentType accidentType;
    private String accidentDetails;

    public Accident toEntity(Map map, User reporter) {
        return Accident.builder()
                .map(map)
                .floor(this.accidentFloor)
                .accidentPoint(this.accidentPoint)
                .accidentType(this.accidentType)
                .eventStatus(EventStatus.REPORTED) // 최초 생성시 "신고" 상태
                .reporter(reporter)
                .details(this.accidentDetails)
                .build();
    }
}
