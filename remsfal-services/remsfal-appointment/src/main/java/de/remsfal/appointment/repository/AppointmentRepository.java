package de.remsfal.appointment.repository;

import java.util.UUID;

import de.remsfal.appointment.entity.AppointmentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/** 
 * Repository for accessing and storing appointment entities.
*/

@ApplicationScoped
public class AppointmentRepository implements PanacheRepositoryBase<AppointmentEntity, UUID> {
    
    
}
