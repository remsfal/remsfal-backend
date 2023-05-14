package de.remsfal.core.model;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ProjectMemberModel extends UserModel {

    public enum UserRole {
        PROPRIETOR(10), // Eigentuemer
        MANAGER(20), // Verwalter
        LESSOR(30), // Vermieter
        CARETAKER(40), // Hausmeister
        CONSULTANT(50), // Auftragnehmer / Berater
        LESSEE(80); // Mieter
        
        private int leadership;
        
        private UserRole(final int leadership) {
            this.leadership = leadership;
        }
        
        public int getLeadershipLevel() {
            return leadership;
        }

        public boolean isPrivileged() {
            if(leadership <= 30) {
                return true;
            } else {
                return false;
            }
        }
    }

    UserRole getRole();

    default boolean isPrivileged() {
        if(getRole() == null) {
            return false;
        } else {
            return getRole().isPrivileged();
        }
    }

}
