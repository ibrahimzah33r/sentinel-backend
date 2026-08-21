package sentinel_backend.event;

import java.time.Instant;

public record SecurityEventResponse(
                Long id,
                String source,
                EventType eventType,
                Severity severity,
                String message,
                String ipAddress,
                Instant timestamp) {
}