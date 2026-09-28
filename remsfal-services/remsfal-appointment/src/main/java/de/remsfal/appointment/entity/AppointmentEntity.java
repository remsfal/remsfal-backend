package de.remsfal.appointment.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import de.remsfal.appointment.AppointmentType;
import de.remsfal.appointment.BookingStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entity representing an appointment in the scheduling system.
 * Stores all core appointment data auch as time, type, status and related craftsman/resource.
 * Also manages timestamps for creation and updates.
 */

@Entity
@Table(name = "appointments")
public class AppointmentEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "craftsman_id", nullable = false)
    public String craftsmanId;

    @Column(name = "resource_id", nullable = false)
    public String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    public AppointmentType type;

    @Column(name = "duration_minutes", nullable = false)
    public int durationMinutes;

    @Column(name = "from_time", nullable = false)
    public LocalDateTime from;

    @Column(name = "to_time", nullable = false)
    public LocalDateTime to;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public BookingStatus status;

    @Column(name = "confirmed_start")
    public LocalDateTime confirmedStart;

    @Column(name = "confirmed_end")
    public LocalDateTime confirmedEnd;
    
    @Column(name = "timezone", nullable = false)
    public String timezone = "UTC";
    
    @Column(name = "cancellation_reason", length = 500)
    public String cancellationReason;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    public WorkingHoursEntity workingHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static List<AppointmentEntity> findConfirmedByCraftsman(String craftsmanId) {
        return list("craftsmanId = ?1 and status = ?2", craftsmanId, BookingStatus.CONFIRMED);
    }
}
