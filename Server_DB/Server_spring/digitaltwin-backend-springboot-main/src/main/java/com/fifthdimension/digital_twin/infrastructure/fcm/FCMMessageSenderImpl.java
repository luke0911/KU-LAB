package com.fifthdimension.digital_twin.infrastructure.fcm;

import com.fifthdimension.digital_twin.pushmsg.application.FCMTokenService;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "FCM Message Sender")
public class FCMMessageSenderImpl implements FCMMessageSender {

    private final FirebaseMessaging firebaseMessaging;
    private final FCMTokenService fcmTokenService;

    private static final int MAX_BATCH_SIZE = 500;

    @Override
    public void sendEventMessageToToken(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                .build();
        try {
            String response = firebaseMessaging.send(message);
            log.info("Successfully sent message to single token: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed for token: {}, reason: {}", token, e.getMessage());
            if (isInvalidToken(e)) {
                fcmTokenService.deactivateToken(token);
                log.warn("Deactivated invalid FCM token: {}", token);
            }
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send FCM message.");
        }
    }

    @Override
    public void sendEventMessageToToken(List<String> tokens, String title, String body) {
        List<List<String>> batches = partitionList(tokens, MAX_BATCH_SIZE);

        for (List<String> batch : batches) {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                log.info("{} messages sent successfully in batch", response.getSuccessCount());

                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        FirebaseMessagingException e = responses.get(i).getException();
                        String failedToken = batch.get(i);
                        log.warn("Failed to send to token: {} - reason: {}", failedToken, e.getMessage());
                        if (isInvalidToken(e)) {
                            fcmTokenService.deactivateToken(failedToken);
                            log.warn("Deactivated invalid FCM token: {}", failedToken);
                        }
                    }
                }

            } catch (FirebaseMessagingException e) {
                log.error("Batch FCM send failed: {}", e.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Batch FCM send failed.");
            }
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException e) {
        return e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT;
    }

    private List<List<String>> partitionList(List<String> tokens, int size) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i += size) {
            result.add(tokens.subList(i, Math.min(i + size, tokens.size())));
        }
        return result;
    }
}
