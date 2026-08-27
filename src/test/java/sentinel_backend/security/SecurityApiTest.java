package sentinel_backend.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void eventsRequireAuthentication() throws Exception {
        mockMvc.perform(
                get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eventStatusUpdateWithoutCsrfIsForbidden()
            throws Exception {

        mockMvc.perform(
                patch("/api/events/99999/status")
                        .param("status", "REVIEWED")
                        .with(user("analyst")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingEventReturnsNotFound()
            throws Exception {

        mockMvc.perform(
                patch("/api/events/99999/status")
                        .param("status", "REVIEWED")
                        .with(user("analyst"))
                        .with(csrf().asHeader()))
                .andExpect(status().isNotFound());
    }
}