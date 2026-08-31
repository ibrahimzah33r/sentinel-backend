package sentinel_backend.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sentinel_backend.error.ResourceNotFoundException;

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

                if (!analyst.isEnabled()) {
                        throw new IllegalArgumentException(
                                        "Invalid credentials");
                }
                if (!passwordEncoder.matches(request.password(), analyst.getPasswordHash())) {
                        throw new IllegalArgumentException("Invalid credentials");
                }

                return new LoginResponse(
                                analyst.getId(),
                                analyst.getUsername(),
                                analyst.getRole());
        }

        public AnalystResponse createAnalyst(
                        CreateAnalystRequest request) {
                if (analystRepository
                                .findByUsername(request.username())
                                .isPresent()) {
                        throw new IllegalArgumentException(
                                        "Username already exists");
                }

                Analyst analyst = new Analyst();

                analyst.setUsername(request.username());
                analyst.setPasswordHash(
                                passwordEncoder.encode(request.password()));
                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(analyst);

                return new AnalystResponse(
                                savedAnalyst.getId(),
                                savedAnalyst.getUsername(),
                                savedAnalyst.getRole(),
                                savedAnalyst.isEnabled());
        }

        public AnalystResponse resetAnalystPassword(
                        Long id,
                        ResetAnalystPasswordRequest request) {
                Analyst analyst = analystRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Analyst not found"));

                analyst.setPasswordHash(
                                passwordEncoder.encode(request.password()));

                Analyst savedAnalyst = analystRepository.save(analyst);

                return new AnalystResponse(
                                savedAnalyst.getId(),
                                savedAnalyst.getUsername(),
                                savedAnalyst.getRole(),
                                savedAnalyst.isEnabled());
        }

        public AnalystResponse setAnalystEnabled(
                        Long id,
                        boolean enabled) {
                Analyst analyst = analystRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Analyst not found"));

                analyst.setEnabled(enabled);

                Analyst savedAnalyst = analystRepository.save(analyst);

                return new AnalystResponse(
                                savedAnalyst.getId(),
                                savedAnalyst.getUsername(),
                                savedAnalyst.getRole(),
                                savedAnalyst.isEnabled());
        }
}