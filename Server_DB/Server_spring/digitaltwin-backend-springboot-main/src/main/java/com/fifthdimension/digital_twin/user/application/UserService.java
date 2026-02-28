package com.fifthdimension.digital_twin.user.application;

import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.user.domain.User;
import com.fifthdimension.digital_twin.user.domain.UserRepository;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import com.fifthdimension.digital_twin.user.dto.IdCheckResDto;
import com.fifthdimension.digital_twin.user.dto.UserCreateReqDto;
import com.fifthdimension.digital_twin.user.dto.UserResDto;
import com.fifthdimension.digital_twin.user.dto.UserUpdateReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "User Service")
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 임시 어드민 가입 코드
    private final String ADMIN_CODE = "fak#!jw#lerh1!$@fakwnle";

    @Transactional
    public UserResDto createUser(UserCreateReqDto userCreateReqDto) {

        if(userCreateReqDto.getRole() == UserRole.ADMIN && !userCreateReqDto.getAdminCode().equals(ADMIN_CODE)){
            throw new CustomException(HttpStatus.FORBIDDEN, "ADMIN Code가 틀렸습니다.");
        }

        // 유저 생성
        try{
            User newUser = userRepository.save(userCreateReqDto.toEntity(passwordEncoder.encode(userCreateReqDto.getPassword())));
            log.info(newUser.toString());
            return UserResDto.from(newUser);
        }catch (Exception e) {
            log.error("유저 추가 실패.");
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public UserResDto getUser(UUID userId) {

        // userId(PK)로 유저 정보 조회
        User user;
        try{
            user = userRepository.findByIdAndIsDeletedIsFalse(userId).orElseThrow();
        }catch (Exception e) {
            log.error("유저가 존재하지 않습니다.");
            throw new CustomException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return UserResDto.from(user);
    }

    public Page<UserResDto> searchUsers(String accountId, String name, Pageable pageable, UserRole userRole) {
        try {

            // ADMIN
            if (userRole == UserRole.ADMIN) {
                UserRole excludedRole = UserRole.MASTER; // Admin은 Master 제외하고 검색

                if ((accountId == null || accountId.isBlank()) && (name == null || name.isBlank())) {
                    return userRepository.findAllByRoleNotAndIsDeletedIsFalse(excludedRole, pageable)
                            .map(UserResDto::from);
                } else if ((accountId != null && !accountId.isBlank()) && (name == null || name.isBlank())) {
                    return userRepository.findAllByAccountIdContainingAndRoleNotAndIsDeletedIsFalse(
                            accountId, excludedRole, pageable).map(UserResDto::from);
                } else if ((accountId == null || accountId.isBlank()) && (name != null && !name.isBlank())) {
                    return userRepository.findAllByNameContainingAndRoleNotAndIsDeletedIsFalse(
                            name, excludedRole, pageable).map(UserResDto::from);
                } else {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "Account ID 혹은 Name 둘 중 하나로만 검색이 가능합니다.");
                }
            }

            // MASTER
            if (userRole == UserRole.MASTER) {
                if ((accountId == null || accountId.isBlank()) && (name == null || name.isBlank())) {
                    return userRepository.findAll(pageable).map(UserResDto::from);
                } else if ((accountId != null && !accountId.isBlank()) && (name == null || name.isBlank())) {
                    return userRepository.findAllByAccountIdContaining(accountId, pageable).map(UserResDto::from);
                } else if ((accountId == null || accountId.isBlank()) && (name != null && !name.isBlank())) {
                    return userRepository.findAllByNameContaining(name, pageable).map(UserResDto::from);
                } else {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "Account ID 혹은 Name 둘 중 하나로만 검색이 가능합니다.");
                }
            }

            // 기타(권한 없음)
            throw new CustomException(HttpStatus.FORBIDDEN, "검색 권한이 없습니다.");
        } catch (Exception e) {
            log.error("[searchUsers] " + e.getMessage(), e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    @Transactional
    public UserResDto updateUser(UUID userId, UUID reqUserId, String role, UserUpdateReqDto userUpdateReqDto) {
        User targetUser;
        try{
            targetUser = userRepository.findByIdAndIsDeletedIsFalse(userId).orElseThrow();
        }catch (Exception e) {
            log.error("타겟 유저가 존재하지 않습니다.");
            throw new CustomException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        // 권한 체크
        boolean isSelf = userId.equals(reqUserId);
        boolean isAdmin = role.equals(UserRole.ADMIN.getValue());
        boolean isMaster = role.equals(UserRole.MASTER.getValue());

        if (!isSelf && !isAdmin && !isMaster) {
            log.error("WORKER가 본인 외 유저를 수정하려고 시도했습니다.");
            throw new CustomException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        String encodedPassword = null;
        String rawPassword = userUpdateReqDto.getPassword();
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            encodedPassword = passwordEncoder.encode(rawPassword);
        }

        try{
            targetUser.updateUserInfo(encodedPassword,
                    userUpdateReqDto.getPhoneNumber(),
                    userUpdateReqDto.getEmail(),
                    userUpdateReqDto.getAddress(),
                    userUpdateReqDto.getPostalCode());
        }catch (Exception e) {
            log.error("유저 정보 업데이트 실패.");
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return UserResDto.from(targetUser);
    }

    @Transactional
    public void deleteUser(UUID userId, UUID reqUserId, String role) {
        User user;
        try{
            user = userRepository.findByIdAndIsDeletedIsFalse(userId).orElseThrow();
        }catch (Exception e) {
            log.error("타겟 유저가 존재하지 않습니다.");
            throw new CustomException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        // 본인 OR ADMIN만 탈퇴 가능
        if( !userId.equals(reqUserId) && role.equals(UserRole.USER.getValue()) ){
            log.error("권한이 없습니다.");
            throw new CustomException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        try{
            user.softDelete(reqUserId);
        }catch (Exception e) {
            log.error("내부 서버 오류 발생");
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public IdCheckResDto idCheck(String accountId) {
        try{
            userRepository.findByAccountId(accountId).orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "아이디 중복 X"));
        }catch (CustomException e) {
            return new IdCheckResDto(false);
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new IdCheckResDto(true);
    }

}
