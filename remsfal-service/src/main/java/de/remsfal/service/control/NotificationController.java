package de.remsfal.service.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class NotificationController {
    
    @Inject
    Logger logger;
    
    public void informUserAboutProjectMembership(final UserModel user) {
        logger.infov("TODO: User {0} will be informed about project membership", user.getEmail());
    }

}
