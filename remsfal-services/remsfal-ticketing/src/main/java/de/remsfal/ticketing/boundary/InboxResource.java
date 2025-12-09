package de.remsfal.ticketing.boundary;

import de.remsfal.core.api.ticketing.InboxEndpoint;
import de.remsfal.core.json.ticketing.InboxMessageJson;
import de.remsfal.ticketing.control.InboxController;
import de.remsfal.ticketing.control.InboxMessageJsonMapper;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import io.quarkus.security.Authenticated;

import java.util.List;

@Authenticated
@RequestScoped
public class InboxResource extends AbstractResource implements InboxEndpoint {

    @Inject
    InboxController controller;

    @Inject
    InboxMessageJsonMapper mapper;

    @Override
    public List<InboxMessageJson> getInboxMessages(String type, Boolean read, String userId) {
        try {
            List<InboxMessageEntity> entities =
                    controller.getInboxMessages(type, read, userId);

            return mapper.toJsonList(entities);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public InboxMessageJson updateMessageStatus(
            @PathParam("messageId") String messageId,
            @QueryParam("read") boolean read
    ) {
        try {
            String userId = principal.getId().toString();

            InboxMessageEntity updated =
                    controller.updateMessageStatus(messageId, read, userId);

            return mapper.toJson(updated);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public void deleteInboxMessage(String messageId) {
        try {
            String userId = principal.getId().toString();
            controller.deleteMessage(messageId, userId);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}