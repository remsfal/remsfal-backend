package de.remsfal.service.control;

import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CommercialControllerTest extends AbstractTest {

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

    @Inject
    CommercialController commercialController;

    @BeforeEach
    void setupTestProjects() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_1)
                .setParameter(2, TestData.PROJECT_TITLE_1)
                .executeUpdate());
    }

    @Test
    void createCommercial_SUCCESS_getCommercial() {
        final String propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final String buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);

        final CommercialModel commercial = TestData.commercialBuilder().build();
        final CommercialModel result = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId, commercial);

        assertNotEquals(commercial.getId(), result.getId());
        assertEquals(commercial.getTitle(), result.getTitle());
        assertEquals(commercial.getLocation(), result.getLocation());
        assertEquals(commercial.getCommercialSpace(), result.getCommercialSpace());
        assertEquals(commercial.getHeatingSpace(), result.getHeatingSpace());

        final String commercialId = entityManager
                .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", String.class)
                .setParameter("title", TestData.COMMERCIAL_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), commercialId);

        final CommercialModel getResult = commercialController
                .getCommercial(TestData.PROJECT_ID, commercialId);

        assertEquals(result, getResult);
    }

    @Test
    void createCommercial_SUCCESS_deleteCommercial() {
        final String propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final String buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);

        final CommercialModel commercial = TestData.commercialBuilder().build();
        final CommercialModel result = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId, commercial);

        final String commercialId = entityManager
                .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", String.class)
                .setParameter("title", TestData.COMMERCIAL_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), commercialId);

        commercialController.deleteCommercial(TestData.PROJECT_ID, commercialId);
        assertThrows(NotFoundException.class, () -> commercialController.getCommercial(TestData.PROJECT_ID, commercialId));
    }

    @Test
    void createCommercial_SUCCESS_updateCommercial() {
        final String propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final String buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);

        final CommercialModel commercial = TestData.commercialBuilder().build();
        final CommercialModel result = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId, commercial);

        final String commercialId = entityManager
                .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", String.class)
                .setParameter("title", TestData.COMMERCIAL_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), commercialId);

        final CommercialModel updateTo = TestData.commercialBuilder2().build();

        final CommercialModel updated = commercialController.updateCommercial(TestData.PROJECT_ID,
                commercialId, updateTo);

        assertNotEquals(updateTo.getId(), updated.getId());
        assertEquals(updateTo.getTitle(), updated.getTitle());
        assertEquals(updateTo.getLocation(), updated.getLocation());
        assertEquals(updateTo.getCommercialSpace(), updated.getCommercialSpace());
        assertEquals(updateTo.getHeatingSpace(), updated.getHeatingSpace());
    }
}
