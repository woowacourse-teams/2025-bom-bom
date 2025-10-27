package news.bombom.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class NotificationConfig {

    @Value("${fcm.service-account-key:}")
    private String serviceAccountKeyPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            if (!StringUtils.hasText(serviceAccountKeyPath)) {
                log.warn("Firebase 서비스 계정 키가 설정되지 않았습니다. 테스트 모드로 실행합니다.");
                return null;
            }

            InputStream serviceAccount = new ClassPathResource(serviceAccountKeyPath).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase 앱이 초기화되었습니다.");
        }

        return FirebaseMessaging.getInstance();
    }
}
