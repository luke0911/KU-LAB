package com.fifthdimension.digital_twin.event.dto;

import com.fifthdimension.digital_twin.event.domain.Accident;
import com.fifthdimension.digital_twin.event.domain.AccidentType;
import com.fifthdimension.digital_twin.event.domain.EventStatus;
import com.fifthdimension.digital_twin.global.entity.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccidentResDto {

    private Long accidentId;
    private Integer mapId;
    private Float accidentFloor;
    private Point accidentPoint;
    private AccidentType accidentType;
    private EventStatus eventStatus;
    private String reporterAccountId;
    private String reporterName;
    private String accidentDetails;

    public static AccidentResDto from(Accident accident) {
        return AccidentResDto.builder()
                .accidentId(accident.getId())
                .mapId(accident.getMap().getId())
                .accidentFloor(accident.getFloor())
                .accidentPoint(accident.getAccidentPoint())
                .accidentType(accident.getAccidentType())
                .eventStatus(accident.getEventStatus())
                .reporterAccountId(accident.getReporter().getAccountId())
                .reporterName(accident.getReporter().getName())
                .accidentDetails(accident.getDetails())
                .build();
    }
}
