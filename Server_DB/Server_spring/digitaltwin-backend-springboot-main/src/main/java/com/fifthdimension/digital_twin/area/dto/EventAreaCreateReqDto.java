package com.fifthdimension.digital_twin.area.dto;

import com.fifthdimension.digital_twin.area.domain.EventArea;
import com.fifthdimension.digital_twin.area.domain.EventAreaType;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.Map;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.fifthdimension.digital_twin.global.util.PolygonUtils.normalizePolygon;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventAreaCreateReqDto {

    @Size(min = 1, max = 100, message = "Area 이름은 50자 이하로 입력해야 합니다.")
    private String areaName;
    private Integer mapId;
    private Float floor;
    private List<Point> points;
    private String areaDescription;
    private EventAreaType eventAreaType;

    public EventArea toEntity(Map map){
        List<Point> normalizedPoints = normalizePolygon(this.points);
        return EventArea.builder()
                .name(this.areaName)
                .map(map)
                .floor(this.floor)
                .points(normalizedPoints)
                .description(this.areaDescription)
                .eventAreaType(this.eventAreaType)
                .build();
    }
}
