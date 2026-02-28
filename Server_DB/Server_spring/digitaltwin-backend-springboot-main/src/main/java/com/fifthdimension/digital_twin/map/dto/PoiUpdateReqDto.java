package com.fifthdimension.digital_twin.map.dto;

import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.PoiCategory;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PoiUpdateReqDto {

    private Long poiId;
    @Size(min = 1, max = 50, message = "POI 이름은 50자 이하로 입력해야 합니다.")
    private String poiName;
    private Float poiFloor;
    private List<Point> poiPoints;
    private PoiCategory poiCategory;
    private String poiDescription;
}
