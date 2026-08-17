package sentinel_backend.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import sentinel_backend.event.SecurityEventRepository.SecurityEventResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class SecurityEventService {

    private final SecurityEventRepository repository;

    public SecurityEventService(SecurityEventRepository repository) {
        this.repository = repository;
    }

    public List<SecurityEventResponse> getAllEvents() {
        return repository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SecurityEvent saveEvent(SecurityEvent event) {
        return repository.save(event);
    }

    public SecurityEventResponse saveEvent(SecurityEventRequest request) {
        SecurityEvent event = fromRequest(request);
        SecurityEvent savedEvent = repository.save(event);

        return toResponse(savedEvent);
    }

    public void deleteEvent(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Security event not found");
        }

        repository.deleteById(id);
    }

    public SecurityEventResponse updateEvent(
            Long id,
            SecurityEventRequest request) {

        SecurityEvent existingEvent = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Security event not found"));

        existingEvent.setSource(request.source());
        existingEvent.setEventType(request.eventType());
        existingEvent.setSeverity(request.severity());
        existingEvent.setMessage(request.message());
        existingEvent.setIpAddress(request.ipAddress());

        SecurityEvent savedEvent = repository.save(existingEvent);

        return toResponse(savedEvent);
    }

    public SecurityEventResponse getEventById(Long id) {
        SecurityEvent event = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Security event not found"));

        return toResponse(event);
    }

    public List<SecurityEventResponse> getEventsBySeverity(Severity severity) {
        return repository.findBySeverityOrderByTimestampDesc(severity)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SecurityEventResponse> getEventsByEventType(EventType eventType) {
        return repository.findByEventTypeOrderByTimestampDesc(eventType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SecurityEventResponse> getEventsBySeverityAndEventType(
            Severity severity,
            EventType eventType) {

        return repository
                .findBySeverityAndEventTypeOrderByTimestampDesc(severity, eventType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<SecurityEventResponse> getEventsPage(int page, int size) {
    PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by("timestamp").descending()
    );

    return repository.findAll(pageRequest)
            .map(this::toResponse);
}
    private SecurityEventResponse toResponse(SecurityEvent event) {
        return new SecurityEventResponse(
                event.getId(),
                event.getSource(),
                event.getEventType(),
                event.getSeverity(),
                event.getMessage(),
                event.getIpAddress(),
                event.getTimestamp());
    }

    private SecurityEvent fromRequest(SecurityEventRequest request) {
        SecurityEvent event = new SecurityEvent();

        event.setSource(request.source());
        event.setEventType(request.eventType());
        event.setSeverity(request.severity());
        event.setMessage(request.message());
        event.setIpAddress(request.ipAddress());

        return event;
    }
}