package com.fifthdimension.digital_twin.user.presentation;

import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.infrastructure.auth.CustomUserDetails;
import com.fifthdimension.digital_twin.user.application.AuthService;
import com.fifthdimension.digital_twin.user.dto.auth.LogInReqDto;
import com.fifthdimension.digital_twin.user.dto.auth.LogoutReqDto;
import com.fifthdimension.digital_twin.user.dto.auth.RefreshReqDto;
import com.fifthdimension.digital_twin.user.dto.auth.TokenResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AuthController")
@Tag(name = "Auth", description = "Auth API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User LogIn")
    @PostMapping("/login")
    public CommonResponse login(
            @RequestBody LogInReqDto logInReqDto
    ){
        return CommonResponse.success(authService.logIn(logInReqDto), "로그인 성공");
    }

    @Operation(summary = "User Logout")
    @PostMapping("/logout")
    public CommonResponse logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody LogoutReqDto dto
    ) {
        authService.logout(userDetails.getUserId(), dto.getRefreshToken());
        return CommonResponse.success("로그아웃이 완료되었습니다.");
    }

    @Operation(summary = "Token 갱신")
    @PostMapping("/refresh")
    public CommonResponse refreshToken(@RequestBody RefreshReqDto dto) {
        String expiredAccessToken = dto.getAccessToken();
        String refreshToken = dto.getRefreshToken();
        TokenResDto tokens = authService.refreshAccessToken(expiredAccessToken, refreshToken);
        return CommonResponse.success(tokens, "토큰 재발급 성공");
    }
}
