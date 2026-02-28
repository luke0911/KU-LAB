package com.fifthdimension.digital_twin.area.dto;

import com.fifthdimension.digital_twin.area.domain.EventAreaType;
import com.fifthdimension.digital_twin.global.entity.Point;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventAreaUpdateReqDto {

    private Long areaId;
    @Size(min = 1, max = 100, message = "Area 이름은 100자 이하로 입력해야 합니다.")
    private String areaName;
    private Float floor;
    private List<Point> points;
    private String areaDescription;
    private EventAreaType eventAreaType;
}
