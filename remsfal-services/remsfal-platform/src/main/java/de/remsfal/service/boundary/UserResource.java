package de.remsfal.service.boundary;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.core.api.UserEndpoint;
import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.UserJson.UserRole;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.ContractorController;
import de.remsfal.service.control.ProjectController;
import de.remsfal.service.control.TenancyController;
import de.remsfal.service.control.UserController;

import org.eclipse.microprofile.metrics.annotation.Timed;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.metrics.MetricUnits;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
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
    TenancyController tenancyController;

    @Override
    @Timed(name = "GetUserTimer", unit = MetricUnits.MILLISECONDS)
    public UserJson getUser() {
        final CustomerModel user = userController.getUser(principal.getId());
        final Set<UserRole> userRoles = getUserRoles(user);
        return UserJson.valueOf(user, userRoles);
    }

    private Set<UserRole> getUserRoles(final UserModel user) {
        final Set<UserRole> userRoles = new HashSet<>();
        if (projectController.getProjects(user, 0, 1).size() > 0) {
            userRoles.add(UserRole.MANAGER);
        }
        if (contractorController.getOrganizations(user).size() > 0) {
            userRoles.add(UserRole.CONTRACTOR);
        }
        if (tenancyController.getTenancies(user).size() > 0) {
            userRoles.add(UserRole.TENANT);
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