package social_mate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				// Áp dụng quy tắc CORS cho tất cả các đường dẫn
				registry.addMapping("/**").allowedOriginPatterns("*")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS").allowedHeaders("*") 
						.allowCredentials(true) // Rất quan trọng để cho phép gửi cookie/token
						.maxAge(3600); // Thời gian cache cho pre-flight request (OPTIONS)
			}
		};
	}
}