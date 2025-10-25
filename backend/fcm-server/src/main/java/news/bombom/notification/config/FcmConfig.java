package news.bombom.fcm.config;

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

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.service-account-key:}")
    private String serviceAccountKeyPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // 테스트용으로 더미 Firebase 앱 초기화
            if (serviceAccountKeyPath == null || serviceAccountKeyPath.isEmpty()) {
                log.warn("Firebase 서비스 계정 키가 설정되지 않았습니다. 테스트 모드로 실행합니다.");
                return null; // 테스트 모드에서는 null 반환
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
