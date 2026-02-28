package com.fifthdimension.digital_twin.pushmsg.presentation;

import com.fifthdimension.digital_twin.global.response.CommonResponse;
import com.fifthdimension.digital_twin.pushmsg.application.PushMessageService;
import com.fifthdimension.digital_twin.pushmsg.dto.PushReqDto;
import com.fifthdimension.digital_twin.pushmsg.dto.PushToGroupReqDto;
import com.fifthdimension.digital_twin.pushmsg.dto.PushToUserReqDto;
import com.fifthdimension.digital_twin.pushmsg.dto.PushToUsersReqDto;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
@Slf4j(topic = "PushMessageController")
@Tag(name = "Push Message", description = "Push Message API")
public class PushMessageController {

    private final PushMessageService pushMessageService;

    @Operation(summary = "Send push to a single user")
    @PostMapping("/user")
    public CommonResponse sendToUser(
            @RequestBody PushToUserReqDto requestDto
    ) {
        pushMessageService.sendToUser(requestDto.getUserId(), requestDto.getTitle(), requestDto.getBody());
        return CommonResponse.success("Push sent to user");
    }

    @Operation(summary = "Send push to multiple users")
    @PostMapping("/users")
    public CommonResponse sendToUsers(
            @RequestBody PushToUsersReqDto requestDto
    ) {
        pushMessageService.sendToUsers(requestDto.getUserIds(), requestDto.getTitle(), requestDto.getBody());
        return CommonResponse.success("Push sent to users");
    }

    @PostMapping("/user-and-admins")
    public CommonResponse sendToUserAndAdmins(@RequestBody PushToUserReqDto dto) {
        pushMessageService.sendToUserAndAdmins(dto.getUserId(), dto.getTitle(), dto.getBody());
        return CommonResponse.success("Push sent to user and admins");
    }

    @PostMapping("/users-and-admins")
    public CommonResponse sendToUsersAndAdmins(@RequestBody PushReqDto dto) {
        pushMessageService.sendToUsersAndAdmins(dto.getTitle(), dto.getBody());
        return CommonResponse.success("Push sent to users and admins");
    }

    @Operation(summary = "Send push to roles and platforms")
    @PostMapping("/group")
    public CommonResponse sendToRolesAndPlatforms(
            @RequestBody PushToGroupReqDto requestDto
    ) {
        pushMessageService.sendToRolesAndPlatforms(
                requestDto.getRoles(),
                requestDto.getPlatforms(),
                requestDto.getTitle(),
                requestDto.getBody()
        );
        return CommonResponse.success("Push sent to roles/platforms group");
    }
}
