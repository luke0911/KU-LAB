package com.fifthdimension.digital_twin.event.presentation;

import com.fifthdimension.digital_twin.event.application.AccidentService;
import com.fifthdimension.digital_twin.event.domain.EventStatus;
import com.fifthdimension.digital_twin.event.dto.AccidentCreateReqDto;
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
@RequestMapping("/api/accidents")
@RequiredArgsConstructor
@Slf4j(topic = "AccidentController")
@Tag(name = "Accidents", description = "Accident API")
public class AccidentController {

    private final AccidentService accidentService;

    @Operation(summary = "Create Accident")
    @PostMapping
    public CommonResponse createAccident(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AccidentCreateReqDto accidentCreateReqDto
    ){
        return CommonResponse.success(accidentService.createAccident(userDetails.getUserId(), accidentCreateReqDto), "Accident 추가 성공");
    }

    @Operation(summary = "Get Accident Info")
    @GetMapping("/{accidentId}")
    public CommonResponse getAccidentInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long accidentId
    ){
        return CommonResponse.success(accidentService.getAccident(accidentId), "Accident 정보 조회 성공");
    }

    @Operation(summary = "Update Accident Event Status")
    @PatchMapping("/{accidentId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse updateAccidentEventStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long accidentId,
            @RequestBody EventStatusUpdateReqDto eventStatusUpdateReqDto
    ){
        return CommonResponse.success(accidentService.updateEventStatus(accidentId, eventStatusUpdateReqDto, userDetails.getUserId()), "Accident Event Status 업데이트 성공");
    }

    @Operation(summary = "Search Accidents")
    @GetMapping("/search")
    public CommonResponse searchAccidents(
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
            pageSize = 10;
        }
        if (page < 1){
            throw new CustomException(HttpStatus.BAD_REQUEST, "Page 번호는 1 이상이어야 합니다.");
        }
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by(Sort.Direction.fromString(direction.name()), sort.getValue()));

        return CommonResponse.success(accidentService.searchAccidents(mapId, eventStatus, pageable), "Accident 검색 성공");
    }
}
