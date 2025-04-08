package aagapp_backend.configuration;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;
    Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
/*    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource("aagapp-6aa05-firebase-adminsdk-fbsvc-c53b98eafe.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("✅ Firebase initialized successfully");
            } else {
                logger.info("✅ Firebase already initialized");
            }
        } catch (IOException e) {
            logger.error("❌ Firebase config load failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Unexpected error during Firebase initialization: {}", e.getMessage(), e);
        }
    }*/


}
