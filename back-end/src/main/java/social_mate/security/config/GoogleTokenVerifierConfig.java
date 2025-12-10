package social_mate.security.config;

import java.util.List; // <-- Import java.util.List
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Configuration
public class GoogleTokenVerifierConfig {

    // Tiêm (inject) danh sách các Client ID từ file properties
    // Spring Boot sẽ tự động chuyển đổi chuỗi "id1,id2,id3" thành List<String>
    @Value("${app.security.google.client-ids}")
    private List<String> googleClientIds;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        // Sử dụng danh sách Client ID đã tiêm vào
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                // Thay vì singletonList, chúng ta truyền toàn bộ danh sách
                .setAudience(googleClientIds) 
                .build();
    }
}