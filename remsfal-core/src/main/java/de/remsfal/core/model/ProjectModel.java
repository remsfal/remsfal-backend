package de.remsfal.core.model;

import java.util.Set;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ProjectModel {
    
    UUID getId();

    String getTitle();

    Set<? extends ProjectMemberModel> getMembers();

}
