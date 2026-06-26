package de.remsfal.core.json;

import java.util.UUID;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ContractorEmployeeModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.validation.PostValidation;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

@Immutable
@ImmutableStyle
@Schema(description = "A contractor employee")
@JsonDeserialize(as = ImmutableContractorEmployeeJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ContractorEmployeeJson implements ContractorEmployeeModel {

    @Null
    @Nullable
    @Override
    public abstract UUID getContractorId();

    @Nullable
    @Override
    public abstract UUID getUserId();

    @Nullable
    @Size(max = 255)
    @Override
    public abstract String getResponsibility();

    @NotBlank(groups = PostValidation.class)
    @Email
    @Size(max = 255)
    @Nullable
    public abstract String getEmail();

    @Nullable
    public abstract String getName();

    @Nullable
    public abstract Boolean isActive();

    @Derived
    @Override
    public UserModel getUser() {
        if (getEmail() == null && getName() == null && isActive() == null) {
            return null;
        }
        return new UserModel() {
            @Override
            public UUID getId() {
                return getUserId();
            }

            @Override
            public String getEmail() {
                return ContractorEmployeeJson.this.getEmail();
            }

            @Override
            public String getName() {
                return ContractorEmployeeJson.this.getName();
            }

            @Override
            public Boolean isActive() {
                return ContractorEmployeeJson.this.isActive();
            }
        };
    }

    public static ContractorEmployeeJson valueOf(final ContractorEmployeeModel model) {
        if (model == null) {
            return null;
        }

        final ImmutableContractorEmployeeJson.Builder builder = ImmutableContractorEmployeeJson.builder()
            .contractorId(model.getContractorId())
            .userId(model.getUserId())
            .responsibility(model.getResponsibility());

        if (model.getUser() != null) {
            final UserModel user = model.getUser();
            builder.email(user.getEmail())
                .name(user.getName())
                .active(user.isActive());
        }

        return builder.build();
    }
}
