package com.fifthdimension.digital_twin.map.dto;

import com.fifthdimension.digital_twin.map.domain.Map;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MapCreateReqDto {

    @Size(min = 1, max = 50, message = "맵 이름은 50자 이하로 입력해야 합니다.")
    private String mapName;

    public Map toEntity(){
        return Map.builder()
                .name(this.mapName)
                .build();
    }
}
