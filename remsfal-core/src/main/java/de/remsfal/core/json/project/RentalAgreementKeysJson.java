package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentalAgreementKeysModel;
import de.remsfal.core.validation.PostValidation;

@Immutable
@ImmutableStyle
@Schema(description = "A key handover record for a rental agreement")
@JsonDeserialize(as = ImmutableRentalAgreementKeysJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentalAgreementKeysJson implements RentalAgreementKeysModel {

    @NotNull(groups = PostValidation.class, message = "Amount of keys is required")
    @Schema(required = true, description = "Number of keys of this type")
    @Override
    public abstract Integer getAmountOfKeys();

    @Nullable
    @Schema(description = "Date the key(s) were issued to the tenant")
    @Override
    public abstract LocalDate getIssuedAt();

    @Nullable
    @Schema(description = "Date the key(s) were returned by the tenant")
    @Override
    public abstract LocalDate getReturnedAt();

    @Nullable
    @Schema(description = "Free text describing the type of key, e.g. front door, mailbox, garage")
    @Override
    public abstract String getKeyType();

    public static RentalAgreementKeysJson valueOf(final RentalAgreementKeysModel model) {
        if (model == null) {
            return null;
        }
        return ImmutableRentalAgreementKeysJson.builder()
            .amountOfKeys(model.getAmountOfKeys())
            .issuedAt(model.getIssuedAt())
            .returnedAt(model.getReturnedAt())
            .keyType(model.getKeyType())
            .build();
    }

}
