package sentinel_backend.auth;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;
        private final AnalystRepository analystRepository;

        public AuthController(
                        AuthService authService,
                        AnalystRepository analystRepository) {
                this.authService = authService;
                this.analystRepository = analystRepository;
        }

       @PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
) {
    LoginResponse response;

    try {
        response = authService.login(request);
    } catch (IllegalArgumentException exception) {
        return ResponseEntity.status(401).build();
    }

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    response.username(),
                    null,
                    List.of()
            );

    SecurityContext securityContext =
            SecurityContextHolder.createEmptyContext();

    securityContext.setAuthentication(authentication);

    httpRequest.getSession(true)
            .setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

    return ResponseEntity.ok(response);
}

        @GetMapping("/me")
        public ResponseEntity<LoginResponse> me(Authentication authentication) {
                return analystRepository.findByUsername(authentication.getName())
                                .map(analyst -> ResponseEntity.ok(
                                                new LoginResponse(
                                                                analyst.getId(),
                                                                analyst.getUsername())))
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @PostMapping("/logout")
        public ResponseEntity<Void> logout(HttpServletRequest request) {
                request.getSession(false).invalidate();

                return ResponseEntity.noContent().build();
        }

        @GetMapping("/csrf")
        public ResponseEntity<CsrfTokenResponse> csrf(CsrfToken csrfToken) {
                return ResponseEntity.ok(
                                new CsrfTokenResponse(csrfToken.getToken()));
        }
}