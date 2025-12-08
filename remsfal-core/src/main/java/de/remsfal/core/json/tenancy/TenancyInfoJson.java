package de.remsfal.core.json.tenancy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.project.TenancyModel;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import jakarta.validation.groups.ConvertGroup;

import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Carl Rix [carl.rix@student.htw-berlin.de]
 */

@Immutable
@ImmutableStyle
@Schema(description = "A tenancy item with information from the manager's view")
@JsonDeserialize(as = ImmutableTenancyInfoJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyInfoJson implements TenancyModel {

  @Null(groups = PostValidation.class)
  @Nullable
  @Override
  public abstract UUID getId();

  @Override
  public abstract LocalDate getStartOfRental();

  @Nullable
  @Override
  public abstract LocalDate getEndOfRental();

  @Valid
  @ConvertGroup(from = PostValidation.class, to = PostValidation.class)
  @ConvertGroup(from = PatchValidation.class, to = PatchValidation.class)
  @Nullable
  @Override
  public abstract List<UserJson> getTenants();

  public static TenancyInfoJson valueOf(TenancyModel model) {
    return ImmutableTenancyInfoJson.builder()
        .id(model.getId())
        .startOfRental(model.getStartOfRental())
        .endOfRental(model.getEndOfRental())
        .tenants(model.getTenants() != null ? model.getTenants().stream()
            .map(UserJson::valueOf)
            .toList() : null)
        .build();
  }
}
