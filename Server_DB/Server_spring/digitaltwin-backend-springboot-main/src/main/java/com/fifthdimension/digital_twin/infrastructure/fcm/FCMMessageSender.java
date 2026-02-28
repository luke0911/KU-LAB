package com.fifthdimension.digital_twin.infrastructure.fcm;

import java.util.List;

public interface FCMMessageSender {
    void sendEventMessageToToken(String token, String title, String body);
    void sendEventMessageToToken(List<String> tokens, String title, String body);
}
