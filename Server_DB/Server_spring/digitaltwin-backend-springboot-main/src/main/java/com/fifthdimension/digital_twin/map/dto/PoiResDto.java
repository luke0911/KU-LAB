package com.fifthdimension.digital_twin.map.dto;

import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.Poi;
import com.fifthdimension.digital_twin.map.domain.PoiCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class PoiResDto {

    private Long poiId;
    private String poiName;
    private Integer mapId;
    private Float poiFloor;
    private List<Point> poiPoints;
    private Point poiCenterPoint;
    private PoiCategory poiCategory;
    private String poiDescription;

    public static PoiResDto from(Poi poi) {
        return PoiResDto.builder()
                .poiId(poi.getId())
                .poiName(poi.getName())
                .mapId(poi.getMap().getId())
                .poiFloor(poi.getFloor())
                .poiPoints(poi.getPoints())
                .poiCenterPoint(poi.getCenterPoint())
                .poiCategory(poi.getCategory())
                .poiDescription(poi.getDescription())
                .build();
    }
}
