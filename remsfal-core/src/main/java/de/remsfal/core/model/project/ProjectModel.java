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
    AddressModel getBillingAddress();

    Set<? extends ProjectMemberModel> getMembers();

    /**
     * The organization that manages this project (the controller in terms of GDPR),
     * or {@code null} if the project is managed by an individual.
     */
    @Nullable
    default UUID getManagingOrganizationId() {
        return null;
    }

    @Nullable
    default String getManagingOrganizationName() {
        return null;
    }

}
