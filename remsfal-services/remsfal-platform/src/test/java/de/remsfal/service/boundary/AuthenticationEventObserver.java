package de.remsfal.service.boundary;

import org.jboss.logging.Logger;

import de.remsfal.service.boundary.authentication.AuthenticationEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

@ApplicationScoped
class AuthenticationEventObserver {

    @Inject
    Logger logger;
    
    private static final Long BLOCKING_TIMEOUT = 1000L;

    private final LinkedBlockingQueue<AuthenticationEvent> updateQueue = new LinkedBlockingQueue<>();

    public void onEvent(@ObservesAsync AuthenticationEvent event) {
        logger.info("Received AuthenticationEvent for user " + event.getEmail());
        updateQueue.add(event);
    }

    public AuthenticationEvent pollLastEvent() throws InterruptedException {
        return updateQueue.poll(BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public int getNumberOfEvents() {
        return updateQueue.size();
    }

}