package sentinel_backend.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/admin/analysts")
public class AdminController {
        private final AuthService authService;

        public AdminController(AuthService authService) {
                this.authService = authService;
        }

        @PostMapping
        public ResponseEntity<AnalystResponse> createAnalyst(
                        @RequestBody CreateAnalystRequest request) {
                return ResponseEntity.ok(
                                authService.createAnalyst(request));
        }

        @PatchMapping("/{id}/password")
        public ResponseEntity<AnalystResponse> resetPassword(
                        @PathVariable Long id,
                        @RequestBody ResetAnalystPasswordRequest request) {
                return ResponseEntity.ok(
                                authService.resetAnalystPassword(
                                                id,
                                                request));
        }

        @PatchMapping("/{id}/enabled")
        public ResponseEntity<AnalystResponse> setEnabled(
                        @PathVariable Long id,
                        @RequestParam boolean enabled) {
                return ResponseEntity.ok(
                                authService.setAnalystEnabled(
                                                id,
                                                enabled));
        }

        @GetMapping
        public ResponseEntity<List<AnalystResponse>> getAnalysts() {
                return ResponseEntity.ok(
                                authService.getAnalysts());
        }

}