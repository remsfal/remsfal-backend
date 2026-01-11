package de.remsfal.core.json.ticketing;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(as = ImmutableUserInfoJson.class)
@Schema(name = "UserInfo", description = "Basic user data for inbox events")
public interface UserInfoJson {

    @Schema(description = "User ID")
    String id();

    @Schema(description = "Email address")
    String email();
}
