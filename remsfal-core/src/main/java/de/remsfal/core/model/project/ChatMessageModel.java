package de.remsfal.core.model.project;

import java.util.Date;

public interface ChatMessageModel {

    String getId();

    ChatSessionModel getChatSession();

    String getChatSessionId();

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
