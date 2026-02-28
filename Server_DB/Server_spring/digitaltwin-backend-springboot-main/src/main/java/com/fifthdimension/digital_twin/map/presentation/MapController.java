package com.fifthdimension.digital_twin.map.presentation;

import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.global.type.DirectionType;
import com.fifthdimension.digital_twin.global.type.SortType;
import com.fifthdimension.digital_twin.infrastructure.auth.CustomUserDetails;
import com.fifthdimension.digital_twin.map.application.MapService;
import com.fifthdimension.digital_twin.map.dto.MapCreateReqDto;
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
@RequestMapping("/api/maps")
@RequiredArgsConstructor
@Slf4j(topic = "MapController")
@Tag(name = "Maps", description = "Map API")
public class MapController {

    private final MapService mapService;

    // Security 추가 후 맵 추가,수정, 삭제는 ADMIN만 가능하게 수정 필요

    @Operation(summary = "Create Map")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse createMap(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MapCreateReqDto mapCreateReqDto) {
        return CommonResponse.success(mapService.createMap(mapCreateReqDto), "맵 정보 추가 성공");
    }

    @Operation(summary = "Get Map Info")
    @GetMapping("/{mapId}")
    public CommonResponse getMapInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer mapId
    ){
        return CommonResponse.success(mapService.getMap(mapId), "맵 정보 조회 성공");
    }

    @Operation(summary = "Search Maps")
    @GetMapping("/search")
    public CommonResponse searchUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "(CREATED_AT - default, UPDATED_AT)")
            @RequestParam(defaultValue = "CREATED_AT", name = "sort") SortType sort,
            @Parameter(description = "(DESC - default, ASC)")
            @RequestParam(defaultValue = "DESC", name = "direction") DirectionType direction
    ) {
        // 페이지 사이즈 허용값만 받도록 처리
        if (pageSize != 10 && pageSize != 30 && pageSize != 100) {
            pageSize = 10;
        }
        if (page < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Page 번호는 1 이상이어야 합니다.");
        }

        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by(Sort.Direction.fromString(direction.name()), sort.getValue()));

        return CommonResponse.success(mapService.searchMaps(name, pageable), "유저 목록 조회 성공");
    }

    @Operation(summary = "Delete Map Info")
    @DeleteMapping("/{mapId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse deleteMapInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "mapId") Integer mapId
    ){
        mapService.deleteMap(mapId, userDetails.getUserId());
        return CommonResponse.success("맵 정보 삭제 성공");
    }
}
