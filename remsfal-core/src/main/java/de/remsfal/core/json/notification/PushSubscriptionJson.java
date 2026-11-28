package de.remsfal.core.json.notification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.remsfal.core.ImmutableStyle;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@Schema(description = "FCM-Token eines Geräts für Push-Benachrichtigungen")
@JsonDeserialize(as = ImmutablePushSubscriptionJson.class)
public abstract class PushSubscriptionJson {

    @NotBlank
    @Schema(description = "FCM-Registrierungstoken", required = true)
    public abstract String getToken();

}
