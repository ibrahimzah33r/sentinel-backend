package sentinel_backend.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import sentinel_backend.event.SecurityEventRepository.SecurityEventResponse;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Import;
import sentinel_backend.TestContainersConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class SecurityEventControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    private SecurityEventResponse createEvent(
            String source,
            EventType eventType,
            Severity severity,
            String message) throws Exception {

        SecurityEventRequest request = new SecurityEventRequest(
                source,
                eventType,
                severity,
                message,
                null);

        String response = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return jsonMapper.readValue(response, SecurityEventResponse.class);
    }

    @Test
    void shouldCreateSecurityEvent() throws Exception {
        SecurityEventRequest request = new SecurityEventRequest(
                "test-server",
                EventType.FAILED_LOGIN,
                Severity.HIGH,
                "Repeated failed login attempts",
                "192.168.1.100");

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("test-server"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldRejectInvalidSecurityEvent() throws Exception {
        SecurityEventRequest request = new SecurityEventRequest(
                "",
                null,
                null,
                "",
                null);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.source").exists())
                .andExpect(jsonPath("$.validationErrors.eventType").exists())
                .andExpect(jsonPath("$.validationErrors.severity").exists())
                .andExpect(jsonPath("$.validationErrors.message").exists());
    }

    @Test

    void shouldGetSecurityEventById() throws Exception {
        SecurityEventRequest request = new SecurityEventRequest(
                "test-server",
                EventType.FAILED_LOGIN,
                Severity.HIGH,
                "Repeated failed login attempts",
                "192.168.1.100");

        String response = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        SecurityEventResponse createdEvent = jsonMapper.readValue(response, SecurityEventResponse.class);

        mockMvc.perform(get("/api/events/" + createdEvent.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdEvent.id()))
                .andExpect(jsonPath("$.source").value("test-server"));
    }

    @Test
    void shouldReturnNotFoundForMissingEvent() throws Exception {
        mockMvc.perform(get("/api/events/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Security event not found"));
    }

    @Test
    void shouldUpdateSecurityEvent() throws Exception {
        SecurityEventRequest createRequest = new SecurityEventRequest(
                "test-server",
                EventType.FAILED_LOGIN,
                Severity.LOW,
                "Original message",
                "192.168.1.100");

        String response = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        SecurityEventResponse createdEvent = jsonMapper.readValue(response, SecurityEventResponse.class);

        SecurityEventRequest updateRequest = new SecurityEventRequest(
                "test-server",
                EventType.FAILED_LOGIN,
                Severity.HIGH,
                "Updated message",
                "192.168.1.100");

        mockMvc.perform(put("/api/events/" + createdEvent.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.message").value("Updated message"));
    }

    @Test
    void shouldDeleteSecurityEvent() throws Exception {
        SecurityEventRequest request = new SecurityEventRequest(
                "delete-test-server",
                EventType.PORT_SCAN,
                Severity.MEDIUM,
                "Event to delete",
                null);

        String response = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        SecurityEventResponse createdEvent = jsonMapper.readValue(response, SecurityEventResponse.class);

        mockMvc.perform(delete("/api/events/" + createdEvent.id()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/" + createdEvent.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFilterEventsBySeverity() throws Exception {
        createEvent(
                "filter-high",
                EventType.FAILED_LOGIN,
                Severity.HIGH,
                "High severity event");

        mockMvc.perform(get("/api/events")
                .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].severity").value(
                        everyItem(is("HIGH"))));
    }

    @Test
    void shouldFilterEventsByEventType() throws Exception {
        createEvent(
                "filter-port-scan",
                EventType.PORT_SCAN,
                Severity.MEDIUM,
                "Port scan event");

        mockMvc.perform(get("/api/events")
                .param("eventType", "PORT_SCAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventType").value(
                        everyItem(is("PORT_SCAN"))));
    }

    @Test
    void shouldFilterEventsBySeverityAndEventType() throws Exception {
        createEvent(
                "combined-filter",
                EventType.FAILED_LOGIN,
                Severity.CRITICAL,
                "Critical failed login");

        mockMvc.perform(get("/api/events")
                .param("severity", "CRITICAL")
                .param("eventType", "FAILED_LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].severity").value(
                        everyItem(is("CRITICAL"))))
                .andExpect(jsonPath("$[*].eventType").value(
                        everyItem(is("FAILED_LOGIN"))));
    }

    @Test
    void shouldReturnPaginatedEvents() throws Exception {
        mockMvc.perform(get("/api/events/page")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldRejectNegativePage() throws Exception {
        mockMvc.perform(get("/api/events/page")
                .param("page", "-1")
                .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Page must be 0 or greater"));
    }

    @Test
    void shouldRejectOversizedPage() throws Exception {
        mockMvc.perform(get("/api/events/page")
                .param("page", "0")
                .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Size must be between 1 and 100"));
    }

}