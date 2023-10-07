package de.remsfal.service.control.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.logging.Logger;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class LoggerProducer {

    @Produces
    public Logger createLogger(final InjectionPoint ip) {
        return Logger.getLogger(ip.getMember().getDeclaringClass());
    }
}
