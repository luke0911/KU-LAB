package com.fifthdimension.digital_twin.pushmsg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushToUsersReqDto {
    private List<UUID> userIds;
    private String title;
    private String body;
}
