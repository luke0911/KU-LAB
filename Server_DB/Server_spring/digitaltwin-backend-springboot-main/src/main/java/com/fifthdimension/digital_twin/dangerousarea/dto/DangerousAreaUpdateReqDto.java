package com.fifthdimension.digital_twin.dangerousarea.dto;

import com.fifthdimension.digital_twin.dangerousarea.domain.Point3D;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DangerousAreaUpdateReqDto {
    private String areaName;
    private Integer areaFloor;
    private String areaType;
    private List<Point3D> areaPoints;
    private String areaDescripting;
    private String entryTime;
    private String exitTime;
}
