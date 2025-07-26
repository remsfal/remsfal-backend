package de.remsfal.chat.boundary;

import de.remsfal.core.api.ticketing.ChatEndpoint;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class ChatResource implements ChatEndpoint {

    @Context
    ResourceContext resourceContext;

    @Inject
    Instance<ChatSessionResource> chatSessionResource;

    @Override
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

}