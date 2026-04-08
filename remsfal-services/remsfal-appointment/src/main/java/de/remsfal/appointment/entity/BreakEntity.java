package de.remsfal.appointment.entity;

import java.time.LocalTime;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a break period within working hours.
 * Used to model time ranges when a craftsman is unavailable due to breaks.
 */

@Entity
@Table(name = "breaks")
public class BreakEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "working_hours_id", nullable = false)
    public WorkingHoursEntity workingHours;

    @Column(name = "start_time", nullable = false)
    public LocalTime start;

    @Column(name = "end_time", nullable = false)
    public LocalTime end;
}
