package de.remsfal.core.model.project;

import de.remsfal.core.model.UserModel;

import java.util.Date;

/**
 * @author: Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
public interface ChatMessageModel {

    String getId();

    ChatSessionModel getChatSession();

    String getChatSessionId();

    UserModel getSender();

    String getSenderId();


    enum ContentType {
        TEXT,
        FILE
    }

    ContentType getContentType();

    String getContent();

    String getUrl();

    Date getTimestamp();

}
