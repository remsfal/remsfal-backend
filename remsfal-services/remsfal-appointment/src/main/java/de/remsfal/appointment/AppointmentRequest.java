package de.remsfal.appointment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object representing an appointment request exchanged via the API.
 * Contains core appointment data including craftsman, type and time constraints.
 */

public class AppointmentRequest {
    public UUID id;
    @NotNull
    public String craftsmanId;
    @NotNull
    public String resourceId;
    @NotNull
    public AppointmentType type;
    @Min(1)
    public int durationMinutes;
    @NotNull
    public LocalDateTime from;
    @NotNull
    public LocalDateTime to;
    @NotNull
    public WorkingHours workingHours;
    public BookingStatus status;
    public LocalDateTime confirmedStart;
    public LocalDateTime confirmedEnd;
    
    // UTC by default
    public String timezone = "UTC";
    
    
    public String cancellationReason;
    
    public ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }
}
