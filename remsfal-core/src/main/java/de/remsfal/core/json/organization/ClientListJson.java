package de.remsfal.core.json.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.CustomerModel;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@ImmutableStyle
@Schema(description = "A list of clients of a contractor organization")
@JsonDeserialize(as = ImmutableClientListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ClientListJson {

    @NotNull
    public abstract List<ClientJson> getClients();

    public abstract Integer getOffset();

    public abstract Long getTotal();

    public static ClientListJson valueOf(final List<? extends CustomerModel> clients, final Integer offset,
        final Long total) {
        final ImmutableClientListJson.Builder builder = ImmutableClientListJson.builder();
        for (CustomerModel client : clients) {
            builder.addClients(ClientJson.valueOf(client));
        }
        return builder.offset(offset).total(total).build();
    }
}
