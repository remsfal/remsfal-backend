package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ContractorModel;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "An organization")
//TODO: ImmutableOrganizationJson wird nicht erkannt
//@JsonDeserialize(as = ImmutableOrganizationJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrganizsationJson implements OrganizationModel {

    @Null(groups = PostValidation.class)
    @NotNull(groups = PatchValidation.class)
    @Override
    public abstract UUID getId();

    @NotBlank(groups = PostValidation.class)
    @Size(max = 255)
    @Override
    public abstract String getName();

    @Pattern(regexp = "^\\+?[0-9]{10,14}$", message = "Phone number must be in E.164 format")
    @Size(max = 15)
    @Override
    public abstract String getPhone();

    @Email
    @Size(max = 255)
    @Override
    public abstract String getEmail();

    @Size(max = 255)
    @Override
    public abstract String getTrade();

    @Nullable
    @Valid
    @Override
    public abstract AddressJson getAddress();

    /**
     * Create a JSON representation from a model.
     *
     * @param model the model
     * @return the JSON representation
     */
    //TODO: ImmutableOrganizationJson wird nicht erkannt
//    public static OrganizsationJson valueOf(final OrganizationModel model) {
//        if (model == null) {
//            return null;
//        }
//
//        final ImmutableOrganizationJson.Builder builder = ImmutableOrganizationJson.builder()
//                .id(model.getId())
//                .name(model.getName())
//                .phone(model.getPhone())
//                .email(model.getEmail())
//                .trade(model.getTrade());
//
//        if (model.getAddress() != null) {
//            builder.address(AddressJson.valueOf(model.getAddress()));
//        }
//
//        return builder.build();
//    }
}
