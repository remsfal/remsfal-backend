package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.test.TestData;

@QuarkusTest
class TenancyControllerTest extends AbstractServiceTest {

    @BeforeEach
    void setupTestProjects() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.PROJECT_TITLE_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_2)
            .setParameter(2, TestData.PROJECT_TITLE_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_3)
            .setParameter(2, TestData.PROJECT_TITLE_3)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_4)
            .setParameter(2, TestData.PROJECT_TITLE_4)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_5)
            .setParameter(2, TestData.PROJECT_TITLE_5)
            .executeUpdate());
    }

    @Test
    void convertBigDecimal_SUCCESS_noLoss() {
        /*
         * final RentEntity rent = new RentEntity(); rent.generateId(); rent.setBillingCycle(BillingCycle.MONTHLY);
         * rent.setFirstPaymentDate(LocalDate.parse("2008-08-02")); rent.setLastPaymentDate(LocalDate.now());
         * rent.setBasicRent(5294.89f); rent.setOperatingCostsPrepayment(4733.3f); rent.setHeatingCostsPrepayment(18237.8231f);
         * 
         * assertNotNull(rent.getId()); assertEquals(BillingCycle.MONTHLY,rent.getBillingCycle());
         * assertEquals("2008-08-02",rent.getFirstPaymentDate().toString()); assertEquals(5294.89f,rent.getBasicRent());
         * assertEquals(4733.3f,rent.getOperatingCostsPrepayment()); assertEquals(18237.82f,rent.getHeatingCostsPrepayment());
         */
    }
    
}
