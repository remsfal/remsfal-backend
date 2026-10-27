package de.remsfal.appointment.entity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity defining the working time range for a craftsman on a specific day.
 * Includes start and end times when the craftsman is available to take appointments.
 */

@Entity
@Table(name = "working_hours")
public class WorkingHoursEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    public AppointmentEntity appointment;

    @Column(name = "start_time", nullable = false)
    public LocalTime start;

    @Column(name = "end_time", nullable = false)
    public LocalTime end;

    @OneToMany(mappedBy = "workingHours", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<BreakEntity> breaks = new ArrayList<>();
}
