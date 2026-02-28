package com.fifthdimension.digital_twin.pushmsg.dto;

import com.fifthdimension.digital_twin.event.domain.DevicePlatform;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushToGroupReqDto {
    private List<UserRole> roles;
    private List<DevicePlatform> platforms;
    private String title;
    private String body;
}
