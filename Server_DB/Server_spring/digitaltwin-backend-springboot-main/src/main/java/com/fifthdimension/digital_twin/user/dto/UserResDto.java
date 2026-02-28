package com.fifthdimension.digital_twin.user.dto;

import com.fifthdimension.digital_twin.user.domain.User;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserResDto {
    private UUID userId;
    private String accountId;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String postalCode;
    private String registrationNumber;
    private LocalDate birthdate;
    private String ageRange;
    private String gender;
    private String nationality;
    private UserRole userRole;

    public static UserResDto from(User user) {
        return UserResDto.builder()
                .userId(user.getId())
                .accountId(user.getAccountId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .address(user.getAddress())
                .postalCode(user.getPostalCode())
                .registrationNumber(user.getRegistrationNumber())
                .birthdate(user.getBirthdate())
                .ageRange(user.getAgeRange())
                .gender(user.getGender())
                .nationality(user.getNationality())
                .userRole(user.getRole())
                .build();
    }
}
