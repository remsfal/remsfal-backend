package de.remsfal.core.model.ticketing;


/**
 * Represents the internal model of an inbox message within the backend.
 */
public interface InboxMessageModel {

    String id();

    String type();

    String contractor();

    String subject();

    String property();

    String tenant();

    String receivedAt();

    boolean read();

    String userId();

    String issueLink();
}

