package com.fifthdimension.digital_twin.pushmsg.application;

import com.fifthdimension.digital_twin.event.domain.DevicePlatform;
import com.fifthdimension.digital_twin.infrastructure.fcm.FCMMessageSender;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Push Message Service")
public class PushMessageService {

    private final FCMTokenService fcmTokenService;
    private final FCMMessageSender fcmMessageSender;

    public void sendToRolesAndPlatforms(
            List<UserRole> roles,
            List<DevicePlatform> platforms,
            String title,
            String body
    ) {
        for (UserRole role : roles) {
            for (DevicePlatform platform : platforms) {
                List<String> tokens = fcmTokenService.getActiveTokensByRoleAndPlatform(role, platform);
                if (!tokens.isEmpty()) {
                    fcmMessageSender.sendEventMessageToToken(tokens, title, body);
                }
            }
        }
    }

    public void sendToUser(
            UUID userId,
            String title,
            String body
    ) {
        List<String> tokens = fcmTokenService.getActiveTokensByUserId(userId);
        log.info("Sending tokens to {}", tokens);
        if (!tokens.isEmpty()) {
            fcmMessageSender.sendEventMessageToToken(tokens, title, body);
        } else {
            log.warn("No active FCM tokens found for userId={}", userId);
        }
    }

    public void sendToUsers(
            List<UUID> userIds,
            String title,
            String body
    ) {
        List<String> tokens = new ArrayList<>();
        for(UUID userId : userIds) {
            List<String> userTokens = fcmTokenService.getActiveTokensByUserId(userId);
            if (!userTokens.isEmpty()) {
                tokens.addAll(userTokens);
            }
        }

        if (!tokens.isEmpty()) {
            fcmMessageSender.sendEventMessageToToken(tokens, title, body);
        } else {
            log.warn("No active FCM tokens found for userIds={}", userIds);
        }
    }

    public void sendToUserAndAdmins(UUID userId, String title, String body) {
        // 1. 유저 토큰
        List<String> userTokens = fcmTokenService.getActiveTokensByUserId(userId);
        // 2. ADMIN Role 토큰
        List<String> adminTokens = fcmTokenService.getActiveTokensByRole(UserRole.ADMIN);

        List<String> tokens = Stream.concat(userTokens.stream(), adminTokens.stream())
                .distinct()
                .toList();

        if (!tokens.isEmpty()) {
            fcmMessageSender.sendEventMessageToToken(tokens, title, body);
        } else {
            log.warn("No tokens found for user {} or admins", userId);
        }
    }

    public void sendToUsersAndAdmins(String title, String body) {
        // USER Role 토큰
        List<String> userTokens = fcmTokenService.getActiveTokensByRole(UserRole.USER);
        // ADMIN Role 토큰
        List<String> adminTokens = fcmTokenService.getActiveTokensByRole(UserRole.ADMIN);

        List<String> tokens = Stream.concat(userTokens.stream(), adminTokens.stream())
                .distinct()
                .toList();

        if (!tokens.isEmpty()) {
            fcmMessageSender.sendEventMessageToToken(tokens, title, body);
        } else {
            log.warn("No tokens found for users or admins");
        }
    }
}
