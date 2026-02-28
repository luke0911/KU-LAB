package com.fifthdimension.digital_twin.user.presentation;

import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.global.type.DirectionType;
import com.fifthdimension.digital_twin.global.type.SortType;
import com.fifthdimension.digital_twin.infrastructure.auth.CustomUserDetails;
import com.fifthdimension.digital_twin.user.application.UserService;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import com.fifthdimension.digital_twin.user.dto.UserCreateReqDto;
import com.fifthdimension.digital_twin.user.dto.UserUpdateReqDto;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j(topic = "UserController")
@Tag(name = "Users", description = "User API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create User")
    @PostMapping
    public CommonResponse createUser(
            @RequestBody @Valid UserCreateReqDto userCreateReqDto) {
        return CommonResponse.success(userService.createUser(userCreateReqDto), "유저 생성 성공!!");
    }

    @Operation(summary = "Get User Info")
    @GetMapping("/{userId}")
    public CommonResponse getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID userId
    ){
        return CommonResponse.success(userService.getUser(userId), "유저 정보 조회 성공.");
    }

    @Operation(summary = "Search Users (ADMIN & MASTER)")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public CommonResponse searchUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "accountId", required = false) String accountId,
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

        return CommonResponse.success(userService.searchUsers(accountId, name, pageable, UserRole.valueOf(userDetails.getRole())), "유저 목록 조회 성공");
    }

    @Operation(summary = "Update User Info")
    @PutMapping("/{userId}")
    public CommonResponse updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID userId,
            @RequestBody @Valid UserUpdateReqDto userUpdateReqDto
    ){
        return CommonResponse.success(userService.updateUser(userId, userDetails.getUserId(), userDetails.getRole(), userUpdateReqDto), "유저 정보 수정 성공");
    }

    @Operation(summary = "Delete/Withdraw User")
    @DeleteMapping("/{userId}")
    public CommonResponse deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "userId") UUID userId
    ){
        userService.deleteUser(userId, userDetails.getUserId(), userDetails.getRole());
        return CommonResponse.success("유저 탈퇴 완료");
    }

    @Operation(summary = "ID duplicate check")
    @GetMapping("/idCheck")
    public CommonResponse duplicateCheck(
            @RequestParam String accountId
    ){
        return CommonResponse.success(userService.idCheck(accountId), "아이디 중복 체크 결과");
    }
}
