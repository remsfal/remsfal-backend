package de.remsfal.core.model;

import java.util.Set;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ProjectModel {
    
    String getId();

    String getTitle();

    Set<? extends ProjectMemberModel> getMembers();

}
