package de.remsfal.core.model;

/**
 * @author Miroslaw Keil [miroslaw.keil@student.htw-berlin.de]
 */
public interface OrganizationEmployeeModel extends UserModel {

    enum PermissionType {
        READ,
        WRITE,
        DELETE
    }

    enum EmployeeRole {
        OWNER(10),
        MANAGER(20),
        STAFF(30);

        private int leadership;

        EmployeeRole(int leadership) {
            this.leadership = leadership;
        }

        public int getLeadershipLevel() {
            return leadership;
        }

        public boolean isPrivileged(PermissionType permissionType) {
            return switch (permissionType) {
                case READ -> leadership <= 30;
                case WRITE -> leadership <= 20;
                case DELETE -> leadership == 10;
            };
        }
    }

    EmployeeRole getEmployeeRole();

    default boolean isPrivileged(PermissionType permissionType) {
        if (getEmployeeRole() == null) {
            return false;
        } else {
            return getEmployeeRole().isPrivileged(permissionType);
        }
    }
}
