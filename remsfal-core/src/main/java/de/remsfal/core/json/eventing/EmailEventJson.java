package de.remsfal.core.json.eventing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
public interface EmailEventJson {

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
