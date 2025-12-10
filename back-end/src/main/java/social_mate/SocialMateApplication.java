package social_mate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import social_mate.security.jwt.JwtProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(value = JwtProperties.class)
public class SocialMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialMateApplication.class, args);
	}

}
