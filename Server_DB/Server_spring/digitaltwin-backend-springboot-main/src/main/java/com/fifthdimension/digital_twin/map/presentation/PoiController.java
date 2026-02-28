package com.fifthdimension.digital_twin.map.presentation;

import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.global.type.DirectionType;
import com.fifthdimension.digital_twin.global.type.SortType;
import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.infrastructure.auth.CustomUserDetails;
import com.fifthdimension.digital_twin.map.application.PoiService;
import com.fifthdimension.digital_twin.map.dto.PoiCreateReqDto;
import com.fifthdimension.digital_twin.map.dto.PoiUpdateReqDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pois")
@RequiredArgsConstructor
@Slf4j(topic = "PoiController")
@Tag(name = "Pois", description = "POI API")
public class PoiController {

    private final PoiService poiService;

    @Operation(summary = "Create Poi")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse createPoi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PoiCreateReqDto poiCreateReqDto
            ){
        return CommonResponse.success(poiService.createPoi(poiCreateReqDto), "POI 정보 추가 성공");
    }

    @Operation(summary = "Get Poi Info")
    @GetMapping("/{poiId}")
    public CommonResponse getPoiInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long poiId
    ){
        return CommonResponse.success(poiService.getPoi(poiId), "POI 정보 조회 성공");
    }

    @Operation(summary = "Get POIs by mapId and floor")
    @GetMapping
    public CommonResponse getPoisByMapIdAndFloor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("mapId") Integer mapId,
            @RequestParam("floor") Float floor
    ) {
        return CommonResponse.success(
                poiService.getPoisByMapIdAndFloor(mapId, floor),
                "POI 리스트 조회 성공"
        );
    }

    @Operation(summary = "Update Poi Info")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse updatePoiInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PoiUpdateReqDto poiUpdateReqDto
    ){
        return CommonResponse.success(poiService.updatePoi(poiUpdateReqDto), "POI 정보 수정 성공");
    }

    @Operation(summary = "Delete Poi Info")
    @DeleteMapping("/{poiId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse deletePoiInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long poiId
    ){
        poiService.deletePoi(poiId, userDetails.getUserId());
        return CommonResponse.success("POI 삭제 완료");
    }

    @Operation(summary = "Search Pois")
    @GetMapping("/search")
    public CommonResponse searchPOIs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "mapId") Integer mapId,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "(CREATED_AT - default, UPDATED_AT)")
            @RequestParam(defaultValue = "CREATED_AT", name = "sort") SortType sort,
            @Parameter(description = "(DESC - default, ASC)")
            @RequestParam(defaultValue = "DESC", name = "direction") DirectionType direction
    ) {
        if (pageSize != 10 && pageSize != 30 && pageSize != 100) {
            pageSize = 10; // 유효하지 않은 페이지 크기인 경우 기본값으로 설정
        }
        if (page < 1){
            throw new CustomException(HttpStatus.BAD_REQUEST, "Page 번호는 1 이상이어야 합니다.");
        }
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by(Sort.Direction.fromString(direction.name()), sort.getValue()));

        return CommonResponse.success(poiService.searchPois(mapId, keyword, pageable), "POI 검색 성공");
    }
}
