package de.remsfal.service.entity.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.core.model.project.ChatSessionModel.Status;
import de.remsfal.core.model.project.ChatSessionModel.ParticipantRole;
import de.remsfal.core.model.project.ChatSessionModel.TaskType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Parameters;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ChatSessionRepository extends AbstractRepository<ChatSessionEntity> {

    protected static final String PARAM_ID = "id";

    @Inject
    ObjectMapper objectMapper;

    public ChatSessionEntity findChatSessionById(final String id) {
        return find("id = :id", Parameters.with(PARAM_ID, id))
                .firstResultOptional()
                .orElseThrow(() -> new NoSuchElementException("ChatSession with ID " + id + " not found"));
    }

    public List<ChatSessionEntity> findChatSessionsByProjectId(final String projectId) {
        return find("projectId = :projectId", Parameters.with("projectId", projectId))
                .list();
    }

    public List<ChatSessionEntity> findChatSessionsByParticipantId(final String participantId) {
        return find("JOIN participants p WHERE KEY(p) = :participantId", Parameters.with("participantId", participantId))
                .list();
    }

    public ParticipantRole findParticipantRole(String sessionId, String participantId) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        ParticipantRole role = session.getParticipants().get(participantId);
        if (role == null) {
            throw new NoSuchElementException("Participant with ID " + participantId + " not found in session " + sessionId);
        }
        return role;
    }

    public List<ChatMessageEntity> exportChatLogs(String sessionId) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        return session.getMessages()
                .stream()
                .sorted(Comparator.comparing(ChatMessageEntity::getTimestamp))
                .toList();
    }

    public String exportChatLogsAsJsonString(String sessionId) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        List<ChatMessageEntity> messages = session.getMessages();

        Map<String, Object> chatSessionJsonMap = new LinkedHashMap<>();
        chatSessionJsonMap.put("CHAT_SESSION_ID", session.getId());
        chatSessionJsonMap.put("TASK_ID", session.getTaskId());
        chatSessionJsonMap.put("PROJECT_ID", session.getProjectId());
        chatSessionJsonMap.put("TASK_TYPE", session.getTaskType());
        chatSessionJsonMap.put("messages", messages.stream()
                .map(this::mapChatMessageToJson)
                .collect(Collectors.toList()));

        try {
            return objectMapper.writeValueAsString(chatSessionJsonMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export chat logs as JSON", e);
        }
    }

    private Map<String, Object> mapChatMessageToJson(ChatMessageEntity message) {
        if (message == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> messageJsonMap = new LinkedHashMap<>();
        messageJsonMap.put("DATETIME", message.getTimestamp());
        messageJsonMap.put("MESSAGE_ID", message.getId());
        messageJsonMap.put("SENDER_ID", message.getSenderId());
        messageJsonMap.put("MEMBER_ROLE", findParticipantRole(message.getChatSessionId(), message.getSenderId()).name());
        messageJsonMap.put("MESSAGE_TYPE", message.getContentType());

        if (message.getContentType() == ChatMessageEntity.ContentType.IMAGE) {
            messageJsonMap.put("MESSAGE_CONTENT", message.getImageUrl());
        } else {
            messageJsonMap.put("MESSAGE_CONTENT", message.getContent());
        }

        return messageJsonMap;
    }

    @Transactional
    public ChatSessionEntity mergeSession(ChatSessionEntity session) {
        return merge(session);
    }

    @Transactional
    public ChatSessionEntity createChatSession(String projectId, String taskId, TaskType taskType,
                                               Map<String, ParticipantRole> participants, Status status) {

        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(UUID.randomUUID().toString());
        session.setProjectId(projectId);
        session.setTaskId(taskId);
        session.setTaskType(taskType);
        session.setParticipants(participants);
        session.setStatus(Optional.ofNullable(status).orElse(Status.OPEN));

        persist(session);
        return session;
    }

    @Transactional
    public ChatSessionEntity updateSessionStatus(String sessionId, Status newStatus) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        session.setStatus(newStatus);
        persist(session);
        return session;
    }

    @Transactional
    public ChatSessionEntity addParticipant(String sessionId, String participantId, ParticipantRole role) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        else if (role == ParticipantRole.INITIATOR && session.getParticipants().values().stream().anyMatch(r -> r == ParticipantRole.INITIATOR)) {
            throw new IllegalArgumentException("Only one participant can have the role INITIATOR");
        }
        else if (session.getParticipants().containsKey(participantId)) {
            throw new IllegalArgumentException("Participant with ID " + participantId + " already exists in session " + sessionId);
        }
        session.getParticipants().put(participantId, role);
        persist(session);
        return session;
    }

    @Transactional
    public ChatSessionEntity changeParticipantRole(String sessionId, String participantId, ParticipantRole newRole) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        if (!session.getParticipants().containsKey(participantId)) {
            throw new NoSuchElementException("Participant with ID " + participantId + " not found in session " + sessionId);
        }
        else if (newRole == ParticipantRole.INITIATOR && session.getParticipants().values().stream().anyMatch(r -> r == ParticipantRole.INITIATOR)) {
            throw new IllegalArgumentException("The role INITIATOR can not be changed or assigned to another participant or more than one participant");
        }
        else if (newRole == null) {
            throw new IllegalArgumentException("Role is required");
        }
        session.getParticipants().put(participantId, newRole);
        persist(session);
        return session;
    }

    @Transactional
    public boolean deleteChatSession(String sessionId) {
        try {
            delete("id = :id", Parameters.with(PARAM_ID, sessionId));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public ChatSessionEntity deleteMember(String sessionId, String participantId) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        if (!session.getParticipants().containsKey(participantId)) {
            throw new NoSuchElementException("Participant with ID " + participantId + " not found in session " + sessionId);
        }
        else if (session.getParticipants().get(participantId) == ParticipantRole.INITIATOR) {
            throw new IllegalArgumentException("The role INITIATOR can not be deleted");
        }
        session.getParticipants().remove(participantId);
        persist(session);
        return session;
    }

    @Transactional
    public ChatSessionEntity updateTaskType(String sessionId, TaskType taskType) {
        ChatSessionEntity session = findChatSessionById(sessionId);
        if (taskType == null) {
            throw new IllegalArgumentException("TaskType is required");
        }
        if (session.getTaskType() == taskType) {
            throw new IllegalArgumentException("TaskType is already set to " + taskType);
        }
        session.setTaskType(taskType);
        persist(session);
        return session;
    }

}
