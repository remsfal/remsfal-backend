package de.remsfal.service.boundary.project;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.AbstractResourceTest;

public abstract class AbstractProjectResourceTest extends AbstractResourceTest {

    protected void setupTestProjects() {
	setupTestUsers();
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.PROJECT_TITLE_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
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
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
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
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
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
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
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
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_5)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
    }

}