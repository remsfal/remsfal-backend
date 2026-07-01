package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.QuotationModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of quotations")
@JsonDeserialize(as = ImmutableQuotationListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationListJson {

    public abstract List<QuotationJson> getItems();

    public static QuotationListJson valueOf(final List<? extends QuotationModel> items) {
        return ImmutableQuotationListJson.builder()
            .items(items.stream().map(QuotationJson::valueOf).toList())
            .build();
    }

}
