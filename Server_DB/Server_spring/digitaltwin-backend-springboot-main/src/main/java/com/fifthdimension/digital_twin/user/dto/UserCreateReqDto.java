package com.fifthdimension.digital_twin.user.dto;

import com.fifthdimension.digital_twin.user.domain.User;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateReqDto {

    @NotBlank(message = "ID를 입력해주세요.")
    @Size(min = 8, max = 20, message = "ID는 8자 이상 20자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "ID는 영어 대소문자와 숫자만 허용됩니다.")
    private String accountId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 9, max = 30, message = "비밀번호는 9자 이상 30자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{9,30}$|"
            + "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{9,30}$|"
            + "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{9,30}$|"
            + "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).{9,30}$|"
            + "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{9,30}$",
            message = "비밀번호는 영대문자, 영소문자, 숫자, 특수문자 중 3종류 이상을 포함하여야 합니다.")
    private String password;

    private String name;

    @Pattern(regexp = "^01(?:0|1|[6-9])\\d{3,4}\\d{4}$",
            message = "유효하지 않은 휴대폰 번호 형식입니다. 01X로 시작하는 10~11자 숫자만 입력해주세요.")
    @Size(min = 10, max = 11, message = "휴대폰 번호는 10자리 또는 11자리여야 합니다.")
    private String phoneNumber;

    @NotBlank(message = "이메일은 필수 입력 값입니다.") // null, "", " " 모두 허용하지 않음
    @Email(message = "유효하지 않은 이메일 형식입니다.") // 이메일 형식 검증
    @Size(max = 255, message = "이메일은 255자를 초과할 수 없습니다.") // 길이 제한
    private String email;

    private String address;

    private String postalCode;

    private String registrationNumber;

    private String nationality;

    private UserRole role;

    private String adminCode;

    public User toEntity(String encodedPassword) {
        User newUser =  User.builder()
                .accountId(this.accountId)
                .password(encodedPassword)
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .address(this.address)
                .postalCode(this.postalCode)
                .registrationNumber(this.registrationNumber)
                .nationality(this.nationality)
                .role(this.role)
                .build();

        // 주민 등록 번호 or 외국인 등록 번호를 통해 생년월일, 나이대, 성별 계산
        newUser.updateBirthdateAgeRangeAndGenderFromRegistrationNumber();
        return newUser;
    }
}
