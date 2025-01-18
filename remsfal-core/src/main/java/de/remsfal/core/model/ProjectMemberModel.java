package de.remsfal.core.model;

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
            if(leadership <= 25) {
                return true;
            } else {
                return false;
            }
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
