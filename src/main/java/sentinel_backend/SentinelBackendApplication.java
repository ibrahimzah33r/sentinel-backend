package sentinel_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class SentinelBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SentinelBackendApplication.class, args);
	}

}
