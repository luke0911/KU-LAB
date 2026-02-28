package com.fifthdimension.digital_twin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserProfileCachingDto implements Serializable {
    String userName;
    String ageRange;
    String gender;
}
