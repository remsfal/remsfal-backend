package de.remsfal.service.control;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApartmentControllerTest extends AbstractTest {

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

    @Inject
    ApartmentController apartmentController;

    @BeforeEach
    void setupTestProjects() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_1)
                .setParameter(2, TestData.PROJECT_TITLE_1)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_2)
                .setParameter(2, TestData.PROJECT_TITLE_2)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_3)
                .setParameter(2, TestData.PROJECT_TITLE_3)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_4)
                .setParameter(2, TestData.PROJECT_TITLE_4)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_5)
                .setParameter(2, TestData.PROJECT_TITLE_5)
                .executeUpdate());
    }


    @Test
    void createApartment_SUCCESS_getApartment() {
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

        final ApartmentModel apartment = TestData.apartmentBuilder().build();
        final ApartmentModel result = apartmentController
                .createApartment(TestData.PROJECT_ID, buildingId, apartment);

        assertNotEquals(apartment.getId(), result.getId());
        assertEquals(apartment.getTitle(), result.getTitle());
        assertEquals(apartment.getLocation(), result.getLocation());
        assertEquals(apartment.getDescription(), result.getDescription());
        assertEquals(apartment.getLivingSpace(), result.getLivingSpace());
        assertEquals(apartment.getUsableSpace(), result.getUsableSpace());

        final String apartmentId = entityManager
                .createQuery("SELECT a.id FROM ApartmentEntity a where a.title = :title", String.class)
                .setParameter("title", TestData.APARTMENT_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), apartmentId);

        final ApartmentModel getResult = apartmentController
                .getApartment(TestData.PROJECT_ID, buildingId, apartmentId);

        assertEquals(result, getResult);
    }
}
