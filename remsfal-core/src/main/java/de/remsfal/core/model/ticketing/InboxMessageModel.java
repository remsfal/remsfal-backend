package de.remsfal.core.model.ticketing;


/**
 * Represents the internal model of an inbox message within the backend.
 */
public interface InboxMessageModel {

    String id();

    String userId();

    String eventType();

    String issueId();

    String title();

    String description();

    String issueType();

    String status();

    String link();

    boolean read();

    String actorEmail();

    String ownerEmail();

    String createdAt();
}

