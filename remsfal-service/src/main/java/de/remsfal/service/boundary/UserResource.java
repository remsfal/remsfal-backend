package de.remsfal.service.boundary;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import de.remsfal.core.UserEndpoint;
import de.remsfal.core.dto.UserJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.UserController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(UserEndpoint.CONTEXT + "/" + UserEndpoint.VERSION + "/" + UserEndpoint.SERVICE)
public class UserResource implements UserEndpoint {

    @Context
    UriInfo uri;

    @Inject
    UserController controller;

    @Override
    public List<UserJson> getUsers() {
        final List<UserJson> users = new ArrayList<>();
        controller.getUsers().forEach(user -> users.add(UserJson.valueOf(user)));
        return users;
    }

    @Override
    public Response createUser(final UserJson user) {
        final String userId = controller.createUser(user);
        final URI location = uri.getAbsolutePathBuilder().path(userId).build();
        return Response.created(location).build();
    }

    @Override
    public UserJson getUser(final String userId) {
        try {
            final UserModel user = controller.getUser(userId);
            return UserJson.valueOf(user);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid user id", e);
        }
    }

    @Override
    public UserJson updateUser(final String userId, final UserJson user) {
        if (user.getId() != null) {
            throw new BadRequestException("User ID should not be set in payload");
        } else {
            //user.setId(userId);
        }
        final UserModel updatedUser = controller.updateUser(user);
        return UserJson.valueOf(updatedUser);
    }

    @Override
    public void deleteUser(final String userId) {
        if (!controller.deleteUser(userId)) {
            throw new NotFoundException();
        }
    }

}