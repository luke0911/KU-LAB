package com.fifthdimension.digital_twin.pushmsg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushReqDto {
    private String title;
    private String body;
}
