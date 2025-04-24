package de.remsfal.chat.boundary;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

import de.remsfal.core.api.chat.ChatEndpoint;

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