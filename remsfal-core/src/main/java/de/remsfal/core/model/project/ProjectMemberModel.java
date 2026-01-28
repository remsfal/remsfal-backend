package de.remsfal.core.model.project;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ProjectMemberModel extends UserModel {

    public enum MemberRole {
        PROPRIETOR(10),   // Eigentuemer
        MANAGER(20),      // Verwalter
        LESSOR(30),       // Vermieter
        STAFF(40),        // Mitarbeiter
        COLLABORATOR(50); // Kollaborateur

        private int leadership;

        private MemberRole(final int leadership) {
            this.leadership = leadership;
        }

        public int getLeadershipLevel() {
            return leadership;
        }

        public boolean isPrivileged() {
            return isPrivileged(MemberRole.MANAGER);
        }

        public boolean isPrivileged(MemberRole minimumRole) {
            return leadership <= minimumRole.getLeadershipLevel();
        }
    }

    MemberRole getRole();

    default boolean isPrivileged() {
        if(getRole() == null) {
            return false;
        } else {
            return getRole().isPrivileged();
        }
    }

}
