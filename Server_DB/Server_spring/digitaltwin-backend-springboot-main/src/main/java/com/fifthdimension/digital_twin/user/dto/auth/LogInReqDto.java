package com.fifthdimension.digital_twin.user.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogInReqDto {

    private String accountId;
    private String password;
}
