package de.remsfal.service.control;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.test.TestData;

import java.util.UUID;

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
class ApartmentControllerTest extends AbstractServiceTest {

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

    @Inject
    ApartmentController apartmentController;

    @BeforeEach
    void setupTestProjects() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO projects (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_1)
                .setParameter(2, TestData.PROJECT_TITLE_1)
                .executeUpdate());
    }


    @Test
    void createApartment_SUCCESS_getApartment() {
        final UUID propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final UUID buildingId = buildingController
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

        final UUID apartmentId = entityManager
                .createQuery("SELECT a.id FROM ApartmentEntity a where a.title = :title", UUID.class)
                .setParameter("title", TestData.APARTMENT_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), apartmentId);

        final ApartmentModel getResult = apartmentController
                .getApartment(TestData.PROJECT_ID, apartmentId);

        assertEquals(result, getResult);
    }

    @Test
    void createApartment_SUCCESS_deleteApartment() {
        final UUID propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final UUID buildingId = buildingController
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

        final UUID apartmentId = entityManager
                .createQuery("SELECT a.id FROM ApartmentEntity a where a.title = :title", UUID.class)
                .setParameter("title", TestData.APARTMENT_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), apartmentId);

        apartmentController.deleteApartment(TestData.PROJECT_ID, apartmentId);
        assertThrows(NotFoundException.class, () -> apartmentController.getApartment(TestData.PROJECT_ID, apartmentId));
    }

    @Test
    void createApartment_SUCCESS_updateApartment() {
        final UUID propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final UUID buildingId = buildingController
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

        final UUID apartmentId = entityManager
                .createQuery("SELECT a.id FROM ApartmentEntity a where a.title = :title", UUID.class)
                .setParameter("title", TestData.APARTMENT_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), apartmentId);

        final ApartmentModel updateTo = TestData.apartmentBuilder2().build();

        final ApartmentModel updated = apartmentController.updateApartment(TestData.PROJECT_ID,
                apartmentId, updateTo);

        assertNotEquals(updateTo.getId(), updated.getId());
        assertEquals(updateTo.getDescription(), updated.getDescription());
        assertEquals(updateTo.getLivingSpace(), updated.getLivingSpace());
        assertEquals(updateTo.getUsableSpace(), updated.getUsableSpace());
        assertEquals(updateTo.getLocation(), updated.getLocation());
        assertEquals(updateTo.getHeatingSpace(), updated.getHeatingSpace());
        assertEquals(updateTo.getTitle(), updated.getTitle());
    }

}
