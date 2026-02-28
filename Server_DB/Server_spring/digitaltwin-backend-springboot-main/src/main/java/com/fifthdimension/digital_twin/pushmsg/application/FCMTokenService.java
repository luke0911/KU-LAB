package com.fifthdimension.digital_twin.pushmsg.application;

import com.fifthdimension.digital_twin.event.domain.DevicePlatform;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.pushmsg.domain.FCMToken;
import com.fifthdimension.digital_twin.pushmsg.domain.FCMTokenRepository;
import com.fifthdimension.digital_twin.pushmsg.dto.FCMTokenReqDto;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "FCMToken Service")
public class FCMTokenService {

    private final FCMTokenRepository fcmTokenRepository;

    @Transactional
    public void saveFCMToken(FCMTokenReqDto reqDto, UUID userId, String userRole) {

        try {
            // 동일한 토큰이 이미 등록돼 있는지 확인
            fcmTokenRepository.findByToken(reqDto.getToken()).ifPresentOrElse(existingToken -> {
                // 이미 있는 토큰이면 무시하거나, 기존 User와 다르면 갱신
                if (!existingToken.getUserId().equals(userId)) {
                    existingToken.updateOwner(userId, UserRole.fromString(userRole));
                    fcmTokenRepository.save(existingToken);
                }
                else{
                    existingToken.setIsActive(true); // 기존 User의 Token 이면 Active 상태로 변경
                }
                log.info("이미 등록된 FCM 토큰입니다. 중복 저장 없이 무시됨: {}", reqDto.getToken());

            }, () -> {
                // 신규 토큰이면 저장
                fcmTokenRepository.save(reqDto.toEntity(userId, userRole));
                log.info("신규 FCM 토큰 저장됨: {}", reqDto.getToken());
            });

        } catch (Exception e) {
            log.error("FCM 토큰 저장 중 오류 발생: {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 저장 중 오류가 발생했습니다.");
        }
    }

    // User의 Token 리스트 반환
    public List<String> getActiveTokensByUserId(UUID userId) {
        try{
            return fcmTokenRepository.findAllByUserIdAndIsActiveTrue(userId).stream().map(FCMToken::getToken).toList();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error occurred while getting active FCM token.");
        }
    }

    // User, Platform으로 Token 리스트 반환
    public List<String> getActiveTokensByUserIdAndPlatform(UUID userId, DevicePlatform platform) {
        try{
            return fcmTokenRepository.findAllByUserIdAndPlatformAndIsActiveTrue(userId, platform).stream().map(FCMToken::getToken).toList();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error occurred while getting active FCM token.");
        }
    }

    // Role으로 Token List 반환
    public List<String> getActiveTokensByRole(UserRole role) {
        try{
            return fcmTokenRepository.findAllByRoleAndIsActiveTrue(role)
                    .stream()
                    .map(FCMToken::getToken)
                    .toList();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error occurred while getting active FCM tokens.");
        }
    }

    // Role, Platform으로 Token List 반환
    public List<String> getActiveTokensByRoleAndPlatform(UserRole role, DevicePlatform platform) {
        try{
            return fcmTokenRepository.findAllByRoleAndPlatformAndIsActiveTrue(role, platform)
                    .stream()
                    .map(FCMToken::getToken)
                    .toList();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error occurred while getting active FCM tokens.");
        }
    }

    @Transactional
    public void deactivateToken(String token) {
        fcmTokenRepository.findByTokenAndIsActiveTrue(token).ifPresent(t -> {
            t.setIsActive(false);
            fcmTokenRepository.save(t);
            log.info("FCM 토큰 비활성화 처리됨: {}", token);
        });
    }

    @Transactional
    public void deleteFCMToken(FCMTokenReqDto fcmTokenReqDto, UUID userId) {
        FCMToken fcmToken = null;
        try{
            fcmToken = fcmTokenRepository.findByUserIdAndTokenAndPlatformAndIsActiveTrue(userId, fcmTokenReqDto.getToken(), fcmTokenReqDto.getPlatform()).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "FCM Token doesn't exist.");
        }

        try{
            fcmTokenRepository.delete(fcmToken);
            log.info("삭제된 FCM 토큰: {}", fcmTokenReqDto.getToken());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error occurred while removing FCM token.");
        }
    }
}
