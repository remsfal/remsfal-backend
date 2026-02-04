package de.remsfal.service.boundary;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.core.api.UserEndpoint;
import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.UserJson.UserContext;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.ContractorController;
import de.remsfal.service.control.ProjectController;
import de.remsfal.service.control.RentalAgreementController;
import de.remsfal.service.control.UserController;

import org.eclipse.microprofile.metrics.annotation.Timed;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.metrics.MetricUnits;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
public class UserResource implements UserEndpoint {

    @Inject
    RemsfalPrincipal principal;

    @Inject
    UserController userController;

    @Inject
    ProjectController projectController;

    @Inject
    ContractorController contractorController;

    @Inject
    RentalAgreementController rentalAgreementController;

    @Override
    @Timed(name = "GetUserTimer", unit = MetricUnits.MILLISECONDS)
    public UserJson getUser() {
        final CustomerModel user = userController.getUser(principal.getId());
        final Set<UserContext> userRoles = getUserContexts(user);
        return UserJson.valueOf(user, userRoles);
    }

    private Set<UserContext> getUserContexts(final UserModel user) {
        final Set<UserContext> userRoles = new HashSet<>();
        if (!projectController.getProjects(user, 0, 1).isEmpty()) {
            userRoles.add(UserContext.MANAGER);
        }
        if (!contractorController.getOrganizations(user).isEmpty()) {
            userRoles.add(UserContext.CONTRACTOR);
        }
        if (!rentalAgreementController.getRentalAgreements(user).isEmpty()) {
            userRoles.add(UserContext.TENANT);
        }
        return userRoles;
    }

    @Override
    @Timed(name = "UpdateUserTimer", unit = MetricUnits.MILLISECONDS)
    public UserJson updateUser(final UserJson user) {
        final CustomerModel updatedUser = userController.updateUser(principal.getId(), user);
        return UserJson.valueOf(updatedUser);
    }

    @Override
    @Timed(name = "DeleteUserTimer", unit = MetricUnits.MILLISECONDS)
    public void deleteUser() {
        if (!userController.deleteUser(principal.getId())) {
            throw new NotFoundException();
        }
    }

}
