package com.fifthdimension.digital_twin.map.dto;

import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.map.domain.Poi;
import com.fifthdimension.digital_twin.map.domain.PoiCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.fifthdimension.digital_twin.global.util.PolygonUtils.normalizePolygon;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PoiCreateReqDto {

    @Size(min = 1, max = 50, message = "POI 이름은 50자 이하로 입력해야 합니다.")
    private String poiName;
    private Integer mapId;
    private Float poiFloor;
    private List<Point> poiPoints;
    @Schema(hidden = true)
    private Point poiCenterPoint;
    private PoiCategory poiCategory;
    private String poiDescription;

    public Poi toEntity(Map map) {
        List<Point> normalizedPoints = normalizePolygon(this.poiPoints);
        return Poi.builder()
                .name(this.poiName)
                .map(map)
                .floor(this.poiFloor)
                .points(normalizedPoints)
                .centerPoint(Point.calculateCenterPoint(poiPoints))
                .category(this.poiCategory)
                .description(this.poiDescription)
                .build();

    }

}