package de.remsfal.core.model.project;

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ProjectOrganizationModel {

    UUID getOrganizationId();

    String getOrganizationName();

    ProjectMemberModel.MemberRole getRole();

}
