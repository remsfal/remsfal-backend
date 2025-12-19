package de.remsfal.core.json.eventing;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableProjectEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface ProjectEventJson {

    UUID getId();

    String getTitle();

}
