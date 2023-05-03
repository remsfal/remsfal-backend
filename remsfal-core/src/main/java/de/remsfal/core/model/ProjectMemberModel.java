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
    }

    UserRole getRole();

}
