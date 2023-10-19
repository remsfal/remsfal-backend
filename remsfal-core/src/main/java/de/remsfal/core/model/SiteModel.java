package de.remsfal.core.model;

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
