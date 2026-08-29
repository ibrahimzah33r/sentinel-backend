package sentinel_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import sentinel_backend.TestContainersConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class AdminAnalystControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalystRepository analystRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        analystRepository.deleteAll();
    }

    @Test
    void anonymousUserCannotCreateAnalyst() throws Exception {
        mockMvc.perform(
                post("/api/admin/analysts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "analyst2",
                                  "password": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void normalAnalystCannotCreateAnalyst() throws Exception {
        mockMvc.perform(
                post("/api/admin/analysts")
                        .with(user("analyst").roles("ANALYST"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "analyst2",
                                  "password": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAnalyst() throws Exception {
        mockMvc.perform(
                post("/api/admin/analysts")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "analyst2",
                                  "password": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.username")
                                .value("analyst2"))
                .andExpect(
                        jsonPath("$.role")
                                .value("ANALYST"))
                .andExpect(
                        jsonPath("$.enabled")
                                .value(true));

        Analyst analyst = analystRepository
                .findByUsername("analyst2")
                .orElseThrow();

        assertThat(analyst.getRole())
                .isEqualTo(AnalystRole.ANALYST);

        assertThat(
                passwordEncoder.matches(
                        "StrongPassword123!",
                        analyst.getPasswordHash()))
                .isTrue();
    }

    @Test
    void normalAnalystCannotResetPassword() throws Exception {
        Analyst analyst = new Analyst(
                "analyst2",
                passwordEncoder.encode("OldPassword123!"));
        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(true);

        Analyst savedAnalyst = analystRepository.save(analyst);

        mockMvc.perform(
                patch(
                        "/api/admin/analysts/"
                                + savedAnalyst.getId()
                                + "/password")
                        .with(user("analyst").roles("ANALYST"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "NewPassword123!"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanResetPassword() throws Exception {
        Analyst analyst = new Analyst(
                "analyst2",
                passwordEncoder.encode("OldPassword123!"));
        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(true);

        Analyst savedAnalyst = analystRepository.save(analyst);

        mockMvc.perform(
                patch(
                        "/api/admin/analysts/"
                                + savedAnalyst.getId()
                                + "/password")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "NewPassword123!"
                                }
                                """))
                .andExpect(status().isOk());

        Analyst updatedAnalyst = analystRepository
                .findById(savedAnalyst.getId())
                .orElseThrow();

        assertThat(
                passwordEncoder.matches(
                        "NewPassword123!",
                        updatedAnalyst.getPasswordHash()))
                .isTrue();
    }

    @Test
    void adminCanDisableAnalyst() throws Exception {
        Analyst analyst = new Analyst(
                "analyst2",
                passwordEncoder.encode("Password123!"));
        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(true);

        Analyst savedAnalyst = analystRepository.save(analyst);

        mockMvc.perform(
                patch(
                        "/api/admin/analysts/"
                                + savedAnalyst.getId()
                                + "/enabled")
                        .param("enabled", "false")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.enabled")
                                .value(false));

        Analyst updatedAnalyst = analystRepository
                .findById(savedAnalyst.getId())
                .orElseThrow();

        assertThat(updatedAnalyst.isEnabled())
                .isFalse();
    }

    @Test
    void disabledAnalystCannotLogin() throws Exception {
        Analyst analyst = new Analyst(
                "analyst2",
                passwordEncoder.encode("Password123!"));
        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(false);

        analystRepository.save(analyst);

        mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "analyst2",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resettingMissingAnalystReturnsNotFound()
            throws Exception {

        mockMvc.perform(
                patch("/api/admin/analysts/99999/password")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "NewPassword123!"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}