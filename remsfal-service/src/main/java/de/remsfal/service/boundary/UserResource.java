package de.remsfal.service.boundary;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import de.remsfal.core.UserEndpoint;
import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.dto.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.AuthController;
import de.remsfal.service.control.UserController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(UserEndpoint.CONTEXT + "/" + UserEndpoint.VERSION + "/" + UserEndpoint.SERVICE)
public class UserResource implements UserEndpoint {

    @Context
    UriInfo uri;

    @Inject
    RemsfalPrincipal principal;

    @Inject
    UserController controller;

    @Inject
    AuthController authController;

    @Override
    public UserJson authenticate(String authHeader) {
        System.out.println("authenticateHeader" + authHeader);
        authController.getClaimsFromJWT(authHeader.replace("Bearer ", ""));
        final UserModel userModel = ImmutableUserJson.builder()
                .id("123")
                .email("email@test.de")
                .name("Test User")
                .build();
        try {
            CustomerModel user = controller.getUser(userModel.getId());
            return UserJson.valueOf(user);
        } catch (IllegalArgumentException e) {
            final CustomerModel newUser;
            newUser = controller.createUser(userModel);
            return UserJson.valueOf(newUser);
        }
    }


    @Override
    public UserJson updateUser(final String userId, final UserJson user) {
        if (user.getId() != null) {
            throw new BadRequestException("User ID should not be set in payload");
        } else {
            //user.setId(userId);
        }
        final CustomerModel updatedUser = controller.updateUser(user);
        return UserJson.valueOf(updatedUser);
    }

    @Override
    public void deleteUser(final String userId) {
        if (!controller.deleteUser(userId)) {
            throw new NotFoundException();
        }
    }

}