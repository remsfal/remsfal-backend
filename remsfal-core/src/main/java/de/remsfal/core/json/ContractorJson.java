package de.remsfal.core.json;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.ContractorModel;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * JSON representation of a contractor.
 */
@Immutable
@ImmutableStyle
@Schema(description = "A contractor")
@JsonDeserialize(as = ImmutableContractorJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ContractorJson implements ContractorModel {

    @Null(groups = PostValidation.class)
    @NotNull(groups = PatchValidation.class)
    @Override
    public abstract  UUID getId();

    @Null
    @Override
    public abstract  UUID getProjectId();

    @NotBlank(groups = PostValidation.class)
    @Size(max = 255)
    @Override
    public abstract  String getCompanyName();

    @Pattern(regexp = "^\\+?[0-9]{10,14}$", message = "Phone number must be in E.164 format")
    @Size(max = 15)
    @Override
    public abstract  String getPhone();

    @Email
    @Size(max = 255)
    @Override
    public abstract  String getEmail();

    @Size(max = 255)
    @Override
    public abstract  String getTrade();

    @Nullable
    @Valid
    @Override
    public abstract  AddressModel getAddress();

    /**
     * Create a JSON representation from a model.
     *
     * @param model the model
     * @return the JSON representation
     */
    public static ContractorJson valueOf(final ContractorModel model) {
        if (model == null) {
            return null;
        }

        final ImmutableContractorJson.Builder builder = ImmutableContractorJson.builder()
            .id(model.getId())
            .projectId(model.getProjectId())
            .companyName(model.getCompanyName())
            .phone(model.getPhone())
            .email(model.getEmail())
            .trade(model.getTrade());

        if (model.getAddress() != null) {
            builder.address(model.getAddress());
        }

        return builder.build();
    }

}
