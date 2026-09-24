package de.remsfal.appointment;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import de.remsfal.appointment.service.AppointmentService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@QuarkusTest
public class CancellationWorkflowTest {

    @Inject
    AppointmentService service;

    @Test
    @Transactional
    public void testCancelOpenAppointmentWithReason() {
        
        AppointmentRequest request = createTestAppointment();
        UUID id = service.create(request);

        String reason = "Client changed their mind";
        service.cancel(id, reason);

        AppointmentRequest cancelled = service.get(id);
        assertEquals(BookingStatus.CANCELLED, cancelled.status);
        assertEquals(reason, cancelled.cancellationReason);
    }

    @Test
    @Transactional
    public void testCancelOpenAppointmentWithoutReason() {

        AppointmentRequest request = createTestAppointment();
        UUID id = service.create(request);

        service.cancel(id, null);

        AppointmentRequest cancelled = service.get(id);
        assertEquals(BookingStatus.CANCELLED, cancelled.status);
        assertNull(cancelled.cancellationReason);
    }

    @Test
    @Transactional
    public void testCancelConfirmedAppointmentWithReason() {
        
        AppointmentRequest request = createTestAppointment();
        UUID id = service.create(request);
        
        var slots = service.computeAvailableSlots(id);
        assertFalse(slots.isEmpty());
        service.confirmBooking(id, slots.get(0));

        AppointmentRequest confirmed = service.get(id);
        assertEquals(BookingStatus.CONFIRMED, confirmed.status);

        String reason = "Emergency - need to reschedule";
        service.cancel(id, reason);

        AppointmentRequest cancelled = service.get(id);
        assertEquals(BookingStatus.CANCELLED, cancelled.status);
        assertEquals(reason, cancelled.cancellationReason);
    }

    @Test
    @Transactional
    public void testCannotCancelDeclinedAppointment() {

        AppointmentRequest request = createTestAppointment();
        UUID id = service.create(request);
        service.decline(id);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.cancel(id, "Trying to cancel declined appointment")
        );

        assertTrue(exception.getMessage().contains("Only OPEN or CONFIRMED appointments can be cancelled"));
    }

    @Test
    @Transactional
    public void testCannotCancelAlreadyCancelledAppointment() {
    
        AppointmentRequest request = createTestAppointment();
        UUID id = service.create(request);
        service.cancel(id, "First cancellation");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.cancel(id, "Second cancellation")
        );

        assertTrue(exception.getMessage().contains("Only OPEN or CONFIRMED appointments can be cancelled"));
    }

    private AppointmentRequest createTestAppointment() {
        AppointmentRequest request = new AppointmentRequest();
        request.craftsmanId = "test-craftsman-" + UUID.randomUUID();
        request.resourceId = "test-resource-" + UUID.randomUUID();
        request.type = AppointmentType.VIEWING;
        request.durationMinutes = 60;
        request.from = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        request.to = LocalDateTime.now().plusDays(1).withHour(17).withMinute(0).withSecond(0).withNano(0);
        request.timezone = "UTC";
        
        WorkingHours wh = new WorkingHours();
        wh.start = LocalTime.of(8, 0);
        wh.end = LocalTime.of(17, 0);
        wh.breaks = java.util.List.of();
        request.workingHours = wh;
        
        return request;
    }
}
