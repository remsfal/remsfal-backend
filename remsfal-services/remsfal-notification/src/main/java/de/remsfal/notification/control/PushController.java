package de.remsfal.notification.control;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class PushController {

    @Inject
    PushProducer producer;

    public void send(String fid, String title, String body) {
        if (producer.send(fid, title, body) == PushProducer.Result.UNREGISTERED) {
            throw new NotFoundException("FID is not registered anymore");
        }
    }

    public void registerFid(UUID userId, String fid) {
        // TODO: persist (insert or update) once the notification database exists
    }
}
