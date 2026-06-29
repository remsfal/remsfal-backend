package de.remsfal.core.model.project;

import de.remsfal.core.model.AddressModel;
import jakarta.annotation.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ProjectModel {
    
    UUID getId();

    String getTitle();

    @Nullable
    String getOwner();

    @Nullable
    String getCareOf();

    @Nullable
    AddressModel getAddress();

    Set<? extends ProjectMemberModel> getMembers();

}
