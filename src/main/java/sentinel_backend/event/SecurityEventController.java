package sentinel_backend.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class SecurityEventController {

    private final SecurityEventService service;

    public SecurityEventController(SecurityEventService service) {
        this.service = service;
    }

    @GetMapping
    public List<SecurityEvent> getAllEvents() {
        return service.getAllEvents();
    }

    @PostMapping
    public SecurityEvent createEvent(@RequestBody SecurityEvent event) {
        return service.saveEvent(event);
    }
}