package com.fifthdimension.digital_twin.pushmsg.presentation;


import com.fifthdimension.digital_twin.pushmsg.application.FCMTokenService;
import com.fifthdimension.digital_twin.pushmsg.dto.FCMTokenReqDto;
import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.infrastructure.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm-token")
@RequiredArgsConstructor
@Slf4j(topic = "FCMTokenController")
@Tag(name = "FCM Token", description = "FCM Token API")
public class FCMTokenController {

    private final FCMTokenService fcmTokenService;

    @Operation(summary = "Save FCM Token")
    @PostMapping
    public CommonResponse saveFCMToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FCMTokenReqDto fcmTokenReqDto
            ){
        fcmTokenService.saveFCMToken(fcmTokenReqDto, userDetails.getUserId(), userDetails.getRole());
        return CommonResponse.success("FCM Token Saved");
    }

    @Operation(summary = "Delete FCM Token")
    @DeleteMapping
    public CommonResponse deleteFCMToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FCMTokenReqDto fcmTokenReqDto
    ){
        fcmTokenService.deleteFCMToken(fcmTokenReqDto, userDetails.getUserId());
        return CommonResponse.success("FCM Token Deleted");
    }

}
