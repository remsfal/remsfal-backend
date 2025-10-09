package de.remsfal.service.boundary.tenancy;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.core.api.tenancy.TaskEndpoint;
import de.remsfal.core.json.tenancy.TaskJson;
import de.remsfal.core.json.tenancy.TaskListJson;
import de.remsfal.core.model.ticketing.TaskModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.TaskModel.Status;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TaskResource extends AbstractTenancyResource implements TaskEndpoint {

    @Override
    public TaskListJson getTasks(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final Status status) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Task functionality moved to ticketing service");
    }

    @Override
    public Response createTask(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final TaskJson task) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Task functionality moved to ticketing service");
    }

    @Override
    public TaskJson getTask(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final UUID taskId) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Task functionality moved to ticketing service");
    }

    @Override
    public TaskJson updateTask(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final UUID taskId, final TaskJson task) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Task functionality moved to ticketing service");
    }

}