package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.QuotationRequestModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of quotation requests")
@JsonDeserialize(as = ImmutableQuotationRequestListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationRequestListJson {

    public abstract List<QuotationRequestJson> getItems();

    public static QuotationRequestListJson valueOf(final List<? extends QuotationRequestModel> items) {
        return ImmutableQuotationRequestListJson.builder()
            .items(items.stream().map(QuotationRequestJson::valueOf).toList())
            .build();
    }

}
