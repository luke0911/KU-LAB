package com.fifthdimension.digital_twin.pushmsg.dto;

import com.fifthdimension.digital_twin.event.domain.DevicePlatform;
import com.fifthdimension.digital_twin.pushmsg.domain.FCMToken;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FCMTokenReqDto {
    private String token;
    private DevicePlatform platform;

    public FCMToken toEntity(UUID userId, String userRole){
        return FCMToken.builder()
                .userId(userId)
                .token(this.token)
                .platform(this.platform)
                .role(UserRole.fromString(userRole))
                .isActive(true)
                .build();
    }
}
