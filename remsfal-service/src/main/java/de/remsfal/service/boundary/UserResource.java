package de.remsfal.service.boundary;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.core.api.UserEndpoint;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.UserController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class UserResource implements UserEndpoint {

    @Inject
    RemsfalPrincipal principal;
    
    @Inject
    UserController controller;

    @Override
    public UserJson getUser() {
        final CustomerModel user = controller.getUser(principal.getId());
        return UserJson.valueOf(user);
    }

    @Override
    public UserJson updateUser(final UserJson user) {
        final CustomerModel updatedUser = controller.updateUser(principal.getId(), user);
        return UserJson.valueOf(updatedUser);
    }

    @Override
    public void deleteUser() {
        if (!controller.deleteUser(principal.getId())) {
            throw new NotFoundException();
        }
    }

}