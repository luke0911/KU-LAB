package com.fifthdimension.digital_twin.user.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LogInResDto {

    private Boolean success;
    private String accessToken;
    private String refreshToken;

}
