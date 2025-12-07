package de.remsfal.core.json.quotation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.quotation.QuotationRequestModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * JSON representation of a list of quotation requests.
 * 
 * @author GitHub Copilot
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of quotation requests")
@JsonDeserialize(as = ImmutableQuotationRequestListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationRequestListJson {

    @Schema(description = "List of quotation requests", required = true)
    public abstract List<QuotationRequestJson> getQuotationRequests();

    public static QuotationRequestListJson valueOf(final List<? extends QuotationRequestModel> models) {
        if (models == null) {
            return null;
        }
        return ImmutableQuotationRequestListJson.builder()
            .quotationRequests(models.stream()
                .map(QuotationRequestJson::valueOf)
                .toList())
            .build();
    }

}
