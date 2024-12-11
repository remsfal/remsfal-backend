package de.remsfal.service.control;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
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
//        runInTransaction(() -> entityManager
//                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
//                .setParameter(1, TestData.PROJECT_ID_2)
//                .setParameter(2, TestData.PROJECT_TITLE_2)
//                .executeUpdate());
//        runInTransaction(() -> entityManager
//                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
//                .setParameter(1, TestData.PROJECT_ID_3)
//                .setParameter(2, TestData.PROJECT_TITLE_3)
//                .executeUpdate());
//        runInTransaction(() -> entityManager
//                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
//                .setParameter(1, TestData.PROJECT_ID_4)
//                .setParameter(2, TestData.PROJECT_TITLE_4)
//                .executeUpdate());
//        runInTransaction(() -> entityManager
//                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
//                .setParameter(1, TestData.PROJECT_ID_5)
//                .setParameter(2, TestData.PROJECT_TITLE_5)
//                .executeUpdate());
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

    @Test
    void createApartment_SUCCESS_deleteApartment() {
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

        apartmentController.deleteApartment(TestData.PROJECT_ID, buildingId, apartmentId);
        assertThrows(NotFoundException.class, () -> apartmentController.getApartment(TestData.PROJECT_ID, buildingId, apartmentId));
    }

    @Test
    void createApartment_SUCCESS_updateApartment() {
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

        final ApartmentModel updateTo = TestData.apartmentBuilder2().build();

        final ApartmentModel updated = apartmentController.updateApartment(TestData.PROJECT_ID, buildingId,
                apartmentId, updateTo);

        assertNotEquals(updateTo.getId(), updated.getId());
        assertEquals(updateTo.getDescription(), updated.getDescription());
        assertEquals(updateTo.getLivingSpace(), updated.getLivingSpace());
        assertEquals(updateTo.getUsableSpace(), updated.getUsableSpace());
        assertEquals(updateTo.getLocation(), updated.getLocation());
        assertEquals(updateTo.getHeatingSpace(), updated.getHeatingSpace());
        assertEquals(updateTo.getTitle(), updated.getTitle());
        assertEquals(apartment.getTenancy(), updated.getTenancy());
    }

}
