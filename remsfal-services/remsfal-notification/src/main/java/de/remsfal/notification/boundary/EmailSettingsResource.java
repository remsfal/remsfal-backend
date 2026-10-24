package de.remsfal.notification.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import de.remsfal.common.boundary.AbstractResource;
import de.remsfal.core.api.notification.EmailSettingsEndpoint;
import de.remsfal.core.json.notification.EmailSettingsJson;
import de.remsfal.notification.control.EmailSettingsController;
import io.quarkus.security.Authenticated;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class EmailSettingsResource extends AbstractResource implements EmailSettingsEndpoint {

    @Inject
    EmailSettingsController controller;

    @Override
    public EmailSettingsJson getEmailSettings() {
        checkManagerPermissions();
        return EmailSettingsJson.valueOf(controller.getSettings(principal.getId()));
    }

    @Override
    public EmailSettingsJson updateEmailSettings(final EmailSettingsJson settings) {
        checkManagerPermissions();
        return EmailSettingsJson.valueOf(controller.updateSettings(principal.getId(), settings));
    }

    /**
     * Issue email settings are only available for managers, i.e. users with a role in at least one project.
     */
    private void checkManagerPermissions() {
        if (principal.getProjectRoles().isEmpty()) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
    }

}
