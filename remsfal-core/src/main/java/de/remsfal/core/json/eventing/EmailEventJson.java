package de.remsfal.core.json.eventing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableEmailEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface EmailEventJson {
    
    public static final String TOPIC = "user-notification";

    public enum EmailEventType {
        USER_REGISTRATION,
        PROJECT_ADMISSION
    }

    /*
     * User information
     */
    UserJson getUser();

    /*
     * User language
     */
    String getLocale();

    /*
     * Type of email, e.g. registration or project membership
     */
    EmailEventType getType();

    /*
     * Link
     */
    String getLink();

}
