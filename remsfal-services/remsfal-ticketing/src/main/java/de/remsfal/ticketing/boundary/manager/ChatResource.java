package de.remsfal.ticketing.boundary.manager;

import de.remsfal.core.api.ticketing.manager.ChatEndpoint;
import de.remsfal.core.json.ticketing.ChatMessageJson;
import de.remsfal.core.json.ticketing.ChatMessageListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.ChatController;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Internal, text-only chat between project members for a single issue. Not reachable by
 * tenants, since {@link #checkProjectIssueAccessPermissions(UUID)} requires a project
 * {@code MemberRole}, which tenants do not have.
 */
@Authenticated
@RequestScoped
public class ChatResource extends AbstractTicketingResource implements ChatEndpoint {

    @Inject
    ChatController chatController;

    @Override
    public ChatMessageListJson getChatMessages(final UUID issueId) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);
        final List<ChatMessageEntity> messages = chatController.getChatMessages(issueId, issue.getProjectId());
        return ChatMessageListJson.valueOf(messages);
    }

    @Override
    public Response createChatMessage(final UUID issueId, final ChatMessageJson message) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);
        final ChatMessageEntity created = chatController.createChatMessage(
            issueId, issue.getProjectId(), principal.getId(), principal.getName(), message.getMessage());

        final URI location = uri.getAbsolutePathBuilder().path(created.getMessageId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(ChatMessageJson.valueOf(created))
            .build();
    }

}
