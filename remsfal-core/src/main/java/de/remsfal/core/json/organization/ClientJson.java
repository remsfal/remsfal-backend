package de.remsfal.core.json.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.CustomerModel;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;
import java.util.UUID;

/**
 * JSON representation of a client (Auftraggeber) of a contractor organization. A client is a project member
 * (proprietor, manager or lessor) of a project in which the organization is registered as contractor.
 */
@Value.Immutable
@ImmutableStyle
@Schema(description = "A client of a contractor organization")
@JsonDeserialize(as = ImmutableClientJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ClientJson {

    @NotNull
    @Schema(description = "Unique identifier of the user")
    public abstract UUID getId();

    @NotNull
    @Schema(description = "Full name of the user, falls back to the email address")
    public abstract String getName();

    @Nullable
    public abstract String getEmail();

    @Nullable
    @Schema(description = "Business phone number, falls back to the mobile phone number")
    public abstract String getPhone();

    /**
     * Create a client representation from a user. The private phone number is never exposed.
     *
     * @param model the user
     * @return the JSON representation
     */
    public static ClientJson valueOf(final CustomerModel model) {
        final String phone = model.getBusinessPhoneNumber() != null
            ? model.getBusinessPhoneNumber()
            : model.getMobilePhoneNumber();
        return ImmutableClientJson.builder()
            .id(model.getId())
            .name(model.getName())
            .email(model.getEmail())
            .phone(phone)
            .build();
    }
}
