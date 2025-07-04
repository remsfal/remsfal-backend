package de.remsfal.notification.boundary;

import de.remsfal.core.api.AddressEndpoint;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.CountryListJson;
import de.remsfal.core.json.UserJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */

@ApplicationScoped
public class MailConsumerResource implements AddressEndpoint {

    @Inject
    Logger logger;

    @Incoming("user-notification-consumer")
    public void consumeUserNotification(UserJson userJson) {
        logger.infov("Received user-notification for {0}", userJson.getEmail());
        logger.debugf("Full user data: %s", userJson);
    }

    @Override
    public CountryListJson getSupportedCountries() {
        return null;
    }

    @Override
    public List<AddressJson> getPossibleCities(final String zipCode) {
        return null;
    }
}
