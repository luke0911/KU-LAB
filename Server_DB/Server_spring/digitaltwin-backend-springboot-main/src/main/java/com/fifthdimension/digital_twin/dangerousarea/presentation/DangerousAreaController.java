package com.fifthdimension.digital_twin.dangerousarea.presentation;

import com.fifthdimension.digital_twin.dangerousarea.application.DangerousAreaService;
import com.fifthdimension.digital_twin.dangerousarea.dto.DangerousAreaCreateReqDto;
import com.fifthdimension.digital_twin.dangerousarea.dto.DangerousAreaResDto;
import com.fifthdimension.digital_twin.dangerousarea.dto.DangerousAreaUpdateReqDto;
import com.fifthdimension.digital_twin.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dangerous-areas")
@RequiredArgsConstructor
public class DangerousAreaController {

    private final DangerousAreaService dangerousAreaService;

    @Operation(summary = "Create Dangerous Area")
    @PostMapping
    public CommonResponse<DangerousAreaResDto> createDangerousArea(@RequestBody DangerousAreaCreateReqDto req) {
        return CommonResponse.success(dangerousAreaService.createDangerousArea(req), "Dangerous Area 생성 성공");
    }

    @Operation(summary = "Get Dangerous Areas by mapId")
    @GetMapping
    public CommonResponse<List<DangerousAreaResDto>> getDangerousAreasByMapId(@RequestParam Integer mapId) {
        return CommonResponse.success(dangerousAreaService.getDangerousAreasByMapId(mapId), "Dangerous Areas 조회 성공");
    }

    @Operation(summary = "Get Dangerous Area by areaId")
    @GetMapping("/{areaId}")
    public CommonResponse<DangerousAreaResDto> getDangerousAreaById(@PathVariable Long areaId) {
        return CommonResponse.success(dangerousAreaService.getDangerousAreaById(areaId), "Dangerous Area 조회 성공");
    }

    @Operation(summary = "Update Dangerous Area by areaId")
    @PutMapping("/{areaId}")
    public CommonResponse<DangerousAreaResDto> updateDangerousArea(
            @PathVariable Long areaId,
            @RequestBody DangerousAreaUpdateReqDto req
    ) {
        return CommonResponse.success(dangerousAreaService.updateDangerousArea(areaId, req), "Dangerous Area 수정 성공");
    }

    @Operation(summary = "Delete Dangerous Area by areaId")
    @DeleteMapping("/{areaId}")
    public CommonResponse<Void> deleteDangerousArea(@PathVariable Long areaId) {
        dangerousAreaService.deleteDangerousArea(areaId);
        return CommonResponse.success(null, "Dangerous Area 삭제 성공");
    }
}
