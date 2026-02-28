package com.fifthdimension.digital_twin.event.presentation;

import com.fifthdimension.digital_twin.event.application.DisasterService;
import com.fifthdimension.digital_twin.event.domain.EventStatus;
import com.fifthdimension.digital_twin.event.dto.DisasterCreateReqDto;
import com.fifthdimension.digital_twin.event.dto.EventStatusUpdateReqDto;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.global.type.DirectionType;
import com.fifthdimension.digital_twin.global.type.SortType;
import com.fifthdimension.digital_twin.infrastructure.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/disasters")
@RequiredArgsConstructor
@Slf4j(topic = "DisasterController")
@Tag(name = "Disasters", description = "Disaster API")
public class DisasterController {

    private final DisasterService disasterService;

    @Operation(summary = "Create Disaster")
    @PostMapping
    public CommonResponse createDisaster(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DisasterCreateReqDto disasterCreateReqDto
            ){
        return CommonResponse.success(disasterService.createDisaster(userDetails.getUserId(), disasterCreateReqDto), "Disaster 추가 성공");
    }

    @Operation(summary = "Get Disaster Info")
    @GetMapping("/{disasterId}")
    public CommonResponse getDisasterInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long disasterId
    ){
        return CommonResponse.success(disasterService.getDisaster(disasterId), "Disaster 정보 조회 성공");
    }

    @Operation(summary = "Update Disaster Event Status")
    @PatchMapping("/{disasterId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse updateDisasterEventStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long disasterId,
            @RequestBody EventStatusUpdateReqDto eventStatusUpdateReqDto
            ){
        return CommonResponse.success(disasterService.updateEventStatus(disasterId, eventStatusUpdateReqDto, userDetails.getUserId()), "Disaster Event Status 업데이트 성공");
    }

    @Operation(summary = "Search Disasters")
    @GetMapping("/search")
    public CommonResponse searchDisasters(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "eventStatus", required = false) EventStatus eventStatus,
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

        return CommonResponse.success(disasterService.searchDisasters(mapId, eventStatus, pageable), "Disaster 검색 성공");
    }
}
