package com.fifthdimension.digital_twin.user.domain;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity(name = "users")
//@SQLRestriction("is_deleted = false")
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "account_id", nullable = false, unique = true, length = 20)
    private String accountId;

    @Column(name = "user_password", nullable = false, length = 60)
    private String password;

    @Column(name = "user_name", nullable = false, length = 20)
    private String name;

    @Column(name = "user_phonenumber", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "user_email", nullable = false)
    private String email;

    @Column(name = "user_address")
    private String address;

    @Column(name = "user_postal_code", length = 20)
    private String postalCode;

    @Column(name = "registration_number", nullable = false, length = 14)
    private String registrationNumber;

    @Column(name = "user_birthdate", nullable = false)
    private LocalDate birthdate;

    @Column(name = "user_age_range", nullable = false, length = 10)
    private String ageRange;

    @Column(name = "user_gender", nullable = false, length = 1)
    private String gender;

    @Column(name = "user_nationality", nullable = false, length = 2)
    private String nationality;


    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @PrePersist
    public void generateUUID(){
        if (id == null){
            id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
        }
    }

    public void updateUserInfo(String encodedPasswordOrNull,
                               String phoneNumber,
                               String email,
                               String address,
                               String postalCode){
        if (encodedPasswordOrNull != null && !encodedPasswordOrNull.isEmpty()) {
            this.password = encodedPasswordOrNull;
        }
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.postalCode = postalCode;
    }

    public void updateBirthdateAgeRangeAndGenderFromRegistrationNumber() {
        if (registrationNumber == null) {
            throw new IllegalArgumentException("Registration number is null");
        }

        // 하이픈 제거
        String cleanedRegNum = registrationNumber.replace("-", "").trim();

        if (cleanedRegNum.length() < 13) {
            throw new IllegalArgumentException("Invalid registration number length");
        }

        String birthDatePart = cleanedRegNum.substring(0, 6);
        char centuryCode = cleanedRegNum.charAt(6);

        int yearPrefix;
        String calculatedGender;

        switch (centuryCode) {
            case '1': case '2':
                yearPrefix = 1900;
                calculatedGender = (centuryCode == '1') ? "M" : "F";
                break;
            case '3': case '4':
                yearPrefix = 2000;
                calculatedGender = (centuryCode == '3') ? "M" : "F";
                break;
            case '5': case '6':
                yearPrefix = 1900;
                calculatedGender = (centuryCode == '5') ? "M" : "F";
                break;
            case '7': case '8':
                yearPrefix = 2000;
                calculatedGender = (centuryCode == '7') ? "M" : "F";
                break;
            default:
                throw new IllegalArgumentException("Invalid century code in registration number");
        }

        int year = yearPrefix + Integer.parseInt(birthDatePart.substring(0, 2));
        String birthDateStr = String.format("%04d", year) + birthDatePart.substring(2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(birthDateStr, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid birthdate in registration number");
        }

        this.birthdate = birthDate;

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        int ageDecade = (age / 10) * 10;
        this.ageRange = ageDecade + "s";

        this.gender = calculatedGender;
    }
}
