package de.remsfal.appointment.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import de.remsfal.appointment.AppointmentRequest;
import de.remsfal.appointment.BookingStatus;
import de.remsfal.appointment.entity.AppointmentEntity;
import de.remsfal.appointment.mapper.AppointmentMapper;
import de.remsfal.appointment.repository.AppointmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

/**
 * Service layer implementing the core business logic for appointment scheduling and booking.
 * Manages persistence, status changes, slot calculations and conflict detection.
 */

@ApplicationScoped
public class AppointmentService {

    private static final Logger LOGGER = Logger.getLogger(AppointmentService.class.getName());

    @Inject
    AppointmentRepository repository;

    private final Object lock = new Object();

    @Transactional
    public UUID create(AppointmentRequest req) {
        req.id = UUID.randomUUID();
        req.status = BookingStatus.OPEN;
        
        AppointmentEntity entity = AppointmentMapper.toEntity(req);
        repository.persist(entity);
        
        LOGGER.info("Created appointment request: " + req.id);
        return req.id;
    }

    public AppointmentRequest get(UUID id) {
        AppointmentEntity entity = repository.findById(id);
        if (entity == null) {
            throw new NotFoundException("Appointment not found: " + id);
        }
        return AppointmentMapper.toDto(entity);
    }

    @Transactional
    public void decline(UUID id) {
        synchronized (lock) {
            AppointmentEntity entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Appointment not found: " + id);
            }
            entity.status = BookingStatus.DECLINED;
            repository.persist(entity);
            LOGGER.info("Declined appointment: " + id);
        }
    }

    @Transactional
    public void cancel(UUID id, String reason) {
        synchronized (lock) {
            AppointmentEntity entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Appointment not found: " + id);
            }
            
            // Only OPEN or CONFIRMED appointments can be cancelled
            if (entity.status != BookingStatus.OPEN && entity.status != BookingStatus.CONFIRMED) {
                throw new IllegalStateException("Only OPEN or CONFIRMED appointments can be cancelled. Current status: " + entity.status);
            }
            
            entity.status = BookingStatus.CANCELLED;
            entity.cancellationReason = reason;
           
            entity.confirmedStart = null;
            entity.confirmedEnd = null;
            repository.persist(entity);
            
            LOGGER.info("Cancelled appointment: " + id + (reason != null ? " with reason: " + reason : ""));
        }
    }

    public List<LocalDateTime> computeAvailableSlots(UUID id) {
        AppointmentEntity entity = repository.findById(id);
        if (entity == null) {
            throw new NotFoundException("Appointment not found: " + id);
        }

        AppointmentRequest r = AppointmentMapper.toDto(entity);
        Duration dur = Duration.ofMinutes(r.durationMinutes);
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime cursor = r.from;

        List<AppointmentEntity> confirmedBookings = 
            AppointmentEntity.findConfirmedByCraftsman(r.craftsmanId);

        while (!cursor.plus(dur).isAfter(r.to)) {
            
            if (!cursor.toLocalDate().equals(cursor.plus(dur).toLocalDate())) {
                cursor = cursor.plusMinutes(15);
                continue;
            }

            final LocalDateTime slotStart = cursor;
            final LocalDateTime slotEnd = cursor.plus(dur);

            if (!r.workingHours.isWithin(slotStart.toLocalTime(), slotEnd.toLocalTime())) {
                cursor = cursor.plusMinutes(15);
                continue;
            }

            boolean conflict = confirmedBookings.stream()
                    .anyMatch(b -> overlaps(slotStart, slotEnd, b.confirmedStart, b.confirmedEnd));

            if (!conflict) {
                slots.add(slotStart);
            }

            cursor = cursor.plusMinutes(15);
        }

        LOGGER.info("Computed " + slots.size() + " available slots for appointment: " + id);
        return slots;
    }

    @Transactional
    public void confirmBooking(UUID id, LocalDateTime slotStart) {
        synchronized (lock) {
            AppointmentEntity entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Appointment not found: " + id);
            }

            AppointmentRequest r = AppointmentMapper.toDto(entity);

            if (r.status != BookingStatus.OPEN) {
                throw new IllegalStateException("Appointment is not open: " + r.status);
            }

            final LocalDateTime selectedSlot = (slotStart != null) ? slotStart : r.from;

            Duration dur = Duration.ofMinutes(r.durationMinutes);
            LocalDateTime slotEnd = selectedSlot.plus(dur);

            if (selectedSlot.isBefore(r.from) || slotEnd.isAfter(r.to)) {
                throw new IllegalStateException("Slot is outside the requested time range");
            }

            if (!r.workingHours.isWithin(selectedSlot.toLocalTime(), slotEnd.toLocalTime())) {
                throw new IllegalStateException("Slot is outside working hours");
            }

            List<AppointmentEntity> confirmedBookings = 
                AppointmentEntity.findConfirmedByCraftsman(r.craftsmanId);
            
            boolean conflict = confirmedBookings.stream()
                    .anyMatch(b -> overlaps(selectedSlot, slotEnd, b.confirmedStart, b.confirmedEnd));

            if (conflict) {
                LOGGER.warning("Booking conflict detected for appointment: " + id);
                throw new IllegalStateException("Slot no longer available");
            }

            entity.status = BookingStatus.CONFIRMED;
            entity.confirmedStart = selectedSlot;
            entity.confirmedEnd = slotEnd;
            repository.persist(entity);

            LOGGER.info("Confirmed booking for appointment: " + id + " at " + selectedSlot);
        }
    }

    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return !aEnd.isEqual(bStart) && !aStart.isEqual(bEnd) && aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}