package com.fifthdimension.digital_twin.dangerousarea.dto;

import com.fifthdimension.digital_twin.dangerousarea.domain.DangerousArea;
import com.fifthdimension.digital_twin.dangerousarea.domain.Point3D;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DangerousAreaCreateReqDto {
    private Integer mapId;
    private String areaName;
    private Integer areaFloor;
    private String areaType;
    private List<Point3D> areaPoints;
    private String areaDescripting;
    private String entryTime;
    private String exitTime;

    public DangerousArea toEntity() {
        return DangerousArea.builder()
                .mapId(mapId)
                .areaName(areaName)
                .areaFloor(areaFloor)
                .areaType(areaType)
                .areaPoints(areaPoints)
                .areaDescripting(areaDescripting)
                .entryTime(entryTime)
                .exitTime(exitTime)
                .build();
    }
}
