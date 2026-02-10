package de.remsfal.core.json.project;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentalAgreementModel;

/**
 * @author Carl Rix [carl.rix@student.htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of rental agreements for a project")
@JsonDeserialize(as = ImmutableRentalAgreementListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentalAgreementListJson {

    public abstract List<RentalAgreementItemJson> getRentalAgreements();

    public static RentalAgreementListJson valueOf(
            final List<? extends RentalAgreementModel> rentalAgreements,
            final Map<UUID, RentalUnitJson> rentalUnitsMap) {
        return ImmutableRentalAgreementListJson.builder()
            .rentalAgreements(rentalAgreements.stream()
                .map(agreement -> RentalAgreementItemJson.valueOf(agreement, rentalUnitsMap))
                .toList())
            .build();
    }

}
