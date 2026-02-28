package com.fifthdimension.digital_twin.area.presentation;

import com.fifthdimension.digital_twin.area.application.EventAreaService;
import com.fifthdimension.digital_twin.area.dto.EventAreaCreateReqDto;
import com.fifthdimension.digital_twin.area.dto.EventAreaUpdateReqDto;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.global.type.DirectionType;
import com.fifthdimension.digital_twin.global.type.SortType;
import com.fifthdimension.digital_twin.infrastructure.auth.CustomUserDetails;
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
@RequestMapping("/api/event-areas")
@RequiredArgsConstructor
@Slf4j(topic = "EventAreaController")
@Tag(name = "Event Areas", description = "Event Area API")
public class EventAreaController {

    private final EventAreaService eventAreaService;

    @Operation(summary = "Create Event Area")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse createEventArea(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid EventAreaCreateReqDto eventAreaCreateReqDto
    ){
        return CommonResponse.success(eventAreaService.createEventArea(eventAreaCreateReqDto), "Event Area 정보 추가 성공");
    }

    @Operation(summary = "Get Event Area Info")
    @GetMapping("/{areaId}")
    public CommonResponse getEventAreaInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long areaId
    ){
        return CommonResponse.success(eventAreaService.getEventArea(areaId), "Event Area 정보 조회 성공");
    }

    @Operation(summary = "Get EventAreas by mapId and floor")
    @GetMapping
    public CommonResponse getEventAreasByMapIdAndFloor(
            @RequestParam("mapId") Integer mapId,
            @RequestParam("floor") Float floor
    ) {
        return CommonResponse.success(
                eventAreaService.getEventAreasByMapIdAndFloor(mapId, floor),
                "EventArea 리스트 조회 성공"
        );
    }

    @Operation(summary = "Update Event Area Info")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse updateEventAreaInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid EventAreaUpdateReqDto eventAreaUpdateReqDto
    ){
        return CommonResponse.success(eventAreaService.updateEventArea(eventAreaUpdateReqDto), "Event Area 정보 수정 성공");
    }

    @Operation(summary = "Delete Event Area Info")
    @DeleteMapping("/{areaId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse deleteEventAreaInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long areaId
    ){
        eventAreaService.deleteEventArea(areaId, userDetails.getUserId());
        return CommonResponse.success("Event Area 삭제 완료");
    }

    @Operation(summary = "Search Event Areas")
    @GetMapping("/search")
    public CommonResponse searchEventAreas(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "mapId") Integer mapId,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "(CREATED_AT - default, UPDATED_AT)")
            @RequestParam(defaultValue = "CREATED_AT", name = "sort") SortType sort,
            @Parameter(description = "(DESC - default, ASC)")
            @RequestParam(defaultValue = "DESC", name = "direction") DirectionType direction
    ){
        if (pageSize != 10 && pageSize != 30 && pageSize != 100) {
            pageSize = 10; // 유효하지 않은 페이지 크기인 경우 기본값으로 설정
        }
        if (page < 1){
            throw new CustomException(HttpStatus.BAD_REQUEST, "Page 번호는 1 이상이어야 합니다.");
        }
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by(Sort.Direction.fromString(direction.name()), sort.getValue()));

        return CommonResponse.success(eventAreaService.searchEventAreas(mapId, name, pageable), "Event Area 검색 성공");
    }
}
