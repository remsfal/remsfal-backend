package de.remsfal.core.model.project;

import de.remsfal.core.model.AddressModel;
import jakarta.annotation.Nullable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface SiteModel {

    @Nullable
    String getId();

    @Nullable
    String getTitle();

    @Nullable
    AddressModel getAddress();

    @Nullable
    String getDescription();

    @Nullable
    Float getUsableSpace();

    @Nullable
    Float getRent();

}
