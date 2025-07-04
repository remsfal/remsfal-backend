package de.remsfal.service.boundary;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.core.api.UserEndpoint;
import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.control.UserController;

import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.MetricUnits;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class UserResource implements UserEndpoint {

    @Inject
    RemsfalPrincipal principal;
    
    @Inject
    UserController controller;

    @Override
    @Timed(name = "GetUserTimer", unit = MetricUnits.MILLISECONDS)
    public UserJson getUser() {
        final CustomerModel user = controller.getUser(principal.getId());
        return UserJson.valueOf(user);
    }

    @Override
    @Timed(name = "UpdateUserTimer", unit = MetricUnits.MILLISECONDS)
    public UserJson updateUser(final UserJson user) {
        final CustomerModel updatedUser = controller.updateUser(principal.getId(), user);
        return UserJson.valueOf(updatedUser);
    }

    @Override
    @Timed(name = "DeleteUserTimer", unit = MetricUnits.MILLISECONDS)
    public void deleteUser() {
        if (!controller.deleteUser(principal.getId())) {
            throw new NotFoundException();
        }
    }

}