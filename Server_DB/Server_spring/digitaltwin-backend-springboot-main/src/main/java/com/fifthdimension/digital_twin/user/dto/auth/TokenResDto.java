package com.fifthdimension.digital_twin.user.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResDto {
    private String accessToken;
    private String refreshToken;
}
