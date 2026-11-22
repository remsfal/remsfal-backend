package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.UserJson;

/**
 * Extension point for changes of a user account, see {@link ProjectEvent} for the semantics.
 */
public sealed interface UserEvent {

    /** Snapshot of the user, taken within the transaction (for a deletion the last state). */
    UserJson user();

    UUID actingUserId();

    default UUID userId() {
        return user().getId();
    }

    /** A (possibly inactive) user was created, e.g. as placeholder for an invited project member. */
    record Created(UserJson user, UUID actingUserId) implements UserEvent {
    }

    /** A user has registered, i.e. logged in for the first time. */
    record Registered(UserJson user, UUID actingUserId) implements UserEvent {
    }

    /** Any change of the user profile. */
    record Updated(UserJson user, UUID actingUserId) implements UserEvent {
    }

    /**
     * The contact data of the user (name, address or phone numbers) has changed, which is relevant for the tenants
     * linked to the user. Fired in addition to {@link Updated}.
     */
    record ContactDataChanged(UserJson user, UUID actingUserId) implements UserEvent {
    }

    /** An additional email was added to the user and has to be verified. */
    record AdditionalEmailAdded(UserJson user, String email, String verificationToken, UUID actingUserId)
        implements UserEvent {
    }

    record Deleted(UserJson user, UUID actingUserId) implements UserEvent {
    }

}
