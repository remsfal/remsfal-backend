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

    /**
     * Lists inbox messages for the authenticated user.
     */
    @Override
    public List<InboxMessageJson> getInboxMessages(
            @QueryParam("read") Boolean read
    ) {
        try {
            String userId = principal.getJwt().getSubject();

            List<InboxMessageEntity> messages =
                    controller.getInboxMessages(read, userId);

            return mapper.toJsonList(messages);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Updates the read flag of a message that belongs to the authenticated user.
     */
    @Override
    public InboxMessageJson updateMessageStatus(
            @PathParam("messageId") String messageId,
            @QueryParam("read") boolean read
    ) {
        try {
            String userId = principal.getJwt().getSubject();

            InboxMessageEntity updated =
                    controller.updateMessageStatus(messageId, read, userId);

            return mapper.toJson(updated);

        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    /**
     * Deletes a message that belongs to the authenticated user.
     */
    @Override
    public void deleteInboxMessage(@PathParam("messageId") String messageId) {
        try {
            String userId = principal.getJwt().getSubject();
            controller.deleteMessage(messageId, userId);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
