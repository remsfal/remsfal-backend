package de.remsfal.core.model.project;

import de.remsfal.core.model.UserModel;

import java.util.Date;

public interface ChatMessageModel {

    String getId();

    ChatSessionModel getChatSession();

    String getChatSessionId();

    UserModel getSender();

    String getSenderId();


     enum ContentType {
        TEXT,
        IMAGE
    }

    ContentType getContentType();

    String getContent();

    String getImageUrl();

    Date getTimestamp();


}
