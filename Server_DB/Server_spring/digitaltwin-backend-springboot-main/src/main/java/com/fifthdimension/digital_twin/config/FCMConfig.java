package com.fifthdimension.digital_twin.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FCMConfig {

    // application.yml에서 설정된 경로 주입
    @Value("${spring.firebase.config-path}")
    private String firebaseConfig;

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        try (FileInputStream serviceAccount = new FileInputStream(firebaseConfig);) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            return FirebaseMessaging.getInstance(app);
        }
    }
}
