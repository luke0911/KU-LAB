package com.fifthdimension.digital_twin.pushmsg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushToUserReqDto {
    private UUID userId;
    private String title;
    private String body;
}
