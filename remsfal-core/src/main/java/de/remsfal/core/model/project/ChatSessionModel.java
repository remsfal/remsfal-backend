package de.remsfal.core.model.project;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ChatSessionModel {

    String getId();

    String getProjectId();

    String getTaskId();

    enum TaskType {
        DEFECT,
        TASK
    }

    TaskType getTaskType();

    Map<String, ParticipantRole> getParticipants();

    enum ParticipantRole {
        INITIATOR,
        HANDLER,
        OBSERVER
    }

    List<? extends ChatMessageModel> getMessages();

    Status getStatus();

    enum Status {
        OPEN,
        CLOSED,
        ARCHIVED
    }

    Date getCreatedAt();

    Date getModifiedAt();

}
