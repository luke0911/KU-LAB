package com.fifthdimension.digital_twin.map.dto;

import com.fifthdimension.digital_twin.map.domain.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MapResDto {

    private Integer mapId;
    private String mapName;

    public static MapResDto from(Map map) {
        return MapResDto.builder()
                .mapId(map.getId())
                .mapName(map.getName())
                .build();
    }
}
