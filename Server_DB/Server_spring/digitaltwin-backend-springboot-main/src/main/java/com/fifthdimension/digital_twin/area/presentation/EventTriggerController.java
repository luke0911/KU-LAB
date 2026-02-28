package com.fifthdimension.digital_twin.area.presentation;

import com.fifthdimension.digital_twin.area.application.EventTriggerService;
import com.fifthdimension.digital_twin.area.dto.EventTriggerCreateReqDto;
import com.fifthdimension.digital_twin.area.dto.EventTriggerUpdateReqDto;
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
@RequestMapping("/api/event-triggers")
@RequiredArgsConstructor
@Slf4j(topic = "EventTriggerController")
@Tag(name = "Event Triggers", description = "Event Trigger API")
public class EventTriggerController {

    private final EventTriggerService eventTriggerService;

    @Operation(summary = "Create Event Trigger")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse createEventTrigger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid EventTriggerCreateReqDto reqDto
    ){
        return CommonResponse.success(
                eventTriggerService.createEventTrigger(reqDto),
                "Event Trigger 정보 추가 성공"
        );
    }

    @Operation(summary = "Get Event Trigger Info")
    @GetMapping("/{triggerId}")
    public CommonResponse getEventTriggerInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long triggerId
    ){
        return CommonResponse.success(
                eventTriggerService.getEventTrigger(triggerId),
                "Event Trigger 정보 조회 성공"
        );
    }

    @Operation(summary = "Update Event Trigger Info")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse updateEventTriggerInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid EventTriggerUpdateReqDto reqDto
    ){
        return CommonResponse.success(
                eventTriggerService.updateEventTrigger(reqDto),
                "Event Trigger 정보 수정 성공"
        );
    }

    @Operation(summary = "Delete Event Trigger")
    @DeleteMapping("/{triggerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse deleteEventTrigger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long triggerId
    ){
        eventTriggerService.deleteEventTrigger(userDetails.getUserId(), triggerId);
        return CommonResponse.success("Event Trigger 삭제 완료");
    }

    @Operation(summary = "Search Event Triggers")
    @GetMapping("/search")
    public CommonResponse searchEventTriggers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "eventAreaId") Long eventAreaId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "(CREATED_AT - default, UPDATED_AT)")
            @RequestParam(defaultValue = "CREATED_AT", name = "sort") SortType sort,
            @Parameter(description = "(DESC - default, ASC)")
            @RequestParam(defaultValue = "DESC", name = "direction") DirectionType direction
    ){
        if (pageSize != 10 && pageSize != 30 && pageSize != 100) {
            pageSize = 10;
        }
        if (page < 1){
            throw new CustomException(HttpStatus.BAD_REQUEST, "Page 번호는 1 이상이어야 합니다.");
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.fromString(direction.name()), sort.getValue()));

        return CommonResponse.success(
                eventTriggerService.searchEventTriggers(eventAreaId, name, pageable),
                "Event Trigger 검색 성공"
        );
    }
}