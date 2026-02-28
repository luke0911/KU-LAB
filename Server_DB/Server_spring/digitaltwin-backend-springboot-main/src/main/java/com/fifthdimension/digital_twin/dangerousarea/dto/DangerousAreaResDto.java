package com.fifthdimension.digital_twin.dangerousarea.dto;

import com.fifthdimension.digital_twin.dangerousarea.domain.DangerousArea;
import com.fifthdimension.digital_twin.dangerousarea.domain.Point3D;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DangerousAreaResDto {
    private Long areaId;
    private Integer mapId;
    private String areaName;
    private Integer areaFloor;
    private String areaType;
    private List<Point3D> areaPoints;
    private String areaDescripting;
    private String entryTime;
    private String exitTime;

    public static DangerousAreaResDto from(DangerousArea area) {
        return DangerousAreaResDto.builder()
                .areaId(area.getAreaId())
                .mapId(area.getMapId())
                .areaName(area.getAreaName())
                .areaFloor(area.getAreaFloor())
                .areaType(area.getAreaType())
                .areaPoints(area.getAreaPoints())
                .areaDescripting(area.getAreaDescripting())
                .entryTime(area.getEntryTime())
                .exitTime(area.getExitTime())
                .build();
    }
}
