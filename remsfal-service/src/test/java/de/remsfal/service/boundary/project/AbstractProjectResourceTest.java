package de.remsfal.service.boundary.project;

import java.util.UUID;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.AbstractResourceTest;

public abstract class AbstractProjectResourceTest extends AbstractResourceTest {

    protected void setupTestProjects() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.PROJECT_TITLE_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_2)
            .setParameter(2, TestData.PROJECT_TITLE_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_2)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_3)
            .setParameter(2, TestData.PROJECT_TITLE_3)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_3)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_4)
            .setParameter(2, TestData.PROJECT_TITLE_4)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_4)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_5)
            .setParameter(2, TestData.PROJECT_TITLE_5)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_5)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
    }

    protected void setupTestProperties() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_1)
            .setParameter(2, TestData.PROJECT_ID_1)
            .setParameter(3, TestData.PROPERTY_TITLE_1)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_1)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_1)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_2)
            .setParameter(2, TestData.PROJECT_ID_1)
            .setParameter(3, TestData.PROPERTY_TITLE_2)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_2)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_2)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, TestData.PROJECT_ID_2)
            .setParameter(3, TestData.PROPERTY_TITLE_1)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_1)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_1)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, TestData.PROJECT_ID_3)
            .setParameter(3, TestData.PROPERTY_TITLE_2)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_2)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_2)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_2)
            .executeUpdate());
    }

}