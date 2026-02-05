package de.remsfal.core.json.project;

import java.util.List;

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

    public abstract List<RentalAgreementJson> getRentalAgreements();

    public static RentalAgreementListJson valueOf(final List<? extends RentalAgreementModel> rentalAgreements) {
        return ImmutableRentalAgreementListJson.builder()
            .rentalAgreements(rentalAgreements.stream()
                .map(RentalAgreementJson::valueOf)
                .toList())
            .build();
    }

}
