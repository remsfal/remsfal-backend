package de.remsfal.core.json.eventing;

import java.util.List;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import jakarta.annotation.Nullable;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableUserEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface UserEventJson {

    String TOPIC = "user-events";

    enum UserEventType {
        USER_DELETED,
        USER_UPDATED
    }

    UserEventType getUserEventType();

    UUID getUserId();

    /**
     * The updated user profile. Only present for {@link UserEventType#USER_UPDATED}.
     */
    @Nullable
    UserJson getUser();

    /**
     * Tenant records linked to this user, each with the project it belongs to. Only present
     * for {@link UserEventType#USER_UPDATED}. May be empty if the user has no linked tenants;
     * consumers decide whether that implies any action.
     */
    @Nullable
    List<AffectedTenantJson> getAffectedTenants();
}
