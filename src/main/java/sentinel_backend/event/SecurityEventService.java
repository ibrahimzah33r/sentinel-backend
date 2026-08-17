package sentinel_backend.event;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityEventService {

    private final SecurityEventRepository repository;

    public SecurityEventService(SecurityEventRepository repository) {
        this.repository = repository;
    }

    public List<SecurityEvent> getAllEvents() {
        return repository.findAll();
    }

    public SecurityEvent saveEvent(SecurityEvent event) {
        return repository.save(event);
    }
}