package sentinel_backend.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AnalystRepository analystRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AnalystRepository analystRepository,
            PasswordEncoder passwordEncoder) {
        this.analystRepository = analystRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Analyst analyst = analystRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), analyst.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return new LoginResponse(
                analyst.getId(),
                analyst.getUsername());
    }
}