package com.fifthdimension.digital_twin.config;

import com.fifthdimension.digital_twin.user.domain.User;
import com.fifthdimension.digital_twin.user.domain.UserRepository;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SeedDataConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String password;

    /*
    ** 최초 Super Admin 계정 생성을 위한 Config
     */

    @Bean
    @Transactional
    public CommandLineRunner seedDataInit() {
        return args -> {

            // superAdmin 유저가 없다면 생성 (accountId로 체크)
            String superAdminId = "superAdmin";
            if (!userRepository.existsByAccountId(superAdminId)) {
                User user = User.builder()
                        .accountId(superAdminId)
                        .password(passwordEncoder.encode(password))
                        .name("Super Admin")
                        .phoneNumber("0220882771")
                        .email("swj8905@korea.ac.kr") // 임시로 원준님 Email
                        .address("Goryeodae-ro 26-gil, Seongbuk-gu, Seoul, Republic of Korea")
                        .postalCode("02856")
                        .registrationNumber("900101-1234567")
                        .nationality("KR")
                        .role(UserRole.MASTER)
                        .build();
                user.updateBirthdateAgeRangeAndGenderFromRegistrationNumber();
                userRepository.save(user);
                log.info("[시드] superAdmin 계정 생성");
            } else {
                log.info("[시드] superAdmin 계정 이미 존재");
            }
        };
    }
}