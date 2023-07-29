package de.remsfal.service.boundary;

import java.net.URI;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
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
        System.out.println("autHeader2 " + authHeader);
        DecodedJWT jwt =  authController.getDecodedJWT(authHeader);
        try {
            CustomerModel user = controller.getUser(jwt.getSubject());
            return UserJson.valueOf(user);
        } catch (NoResultException e) {
            System.out.println("emailll" + jwt.getClaim("email").asString());
            final UserModel userModel = ImmutableUserJson.builder()
                    .id(jwt.getSubject())
                    .email(jwt.getClaim("email").asString())
                    .name(jwt.getClaim("name").asString())
                    .build();
            final CustomerModel newUser = controller.createUser(userModel);
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