package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.BuildingEntity;

@QuarkusTest
class BuildingControllerTest extends AbstractTest {

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

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
    void createBuilding_FAILED_noProjectNoProperty() {
        final String buildingId = propertyController
            .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(buildingId);

        final BuildingModel building = TestData.buildingBuilder()
        	.id(null)
        	.address(TestData.addressBuilder().build())
            .build();
        
        assertThrows(ConstraintViolationException.class,
            () -> buildingController.createBuilding(null, buildingId, building));
        assertThrows(ConstraintViolationException.class,
            () -> buildingController.createBuilding(TestData.PROJECT_ID, null, building));
    }
    
    @Test
    void createBuilding_SUCCESS_idGenerated() {
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId());

        final BuildingModel building = TestData.buildingBuilder()
        	.id(null)
            .address(TestData.addressBuilder().build())
            .build();
        
        final BuildingModel result = buildingController.createBuilding(TestData.PROJECT_ID, property.getId(), building);
        
        assertNotEquals(building.getId(), result.getId());
        assertEquals(building.getTitle(), result.getTitle());
        assertEquals(building.getAddress().getStreet(), result.getAddress().getStreet());
        assertEquals(building.getAddress().getCity(), result.getAddress().getCity());
        assertEquals(building.getAddress().getProvince(), result.getAddress().getProvince());
        assertEquals(building.getAddress().getZip(), result.getAddress().getZip());
        assertEquals(building.getDescription(), result.getDescription());
        assertEquals(building.getLivingSpace(), result.getLivingSpace());
        assertEquals(building.getCommercialSpace(), result.getCommercialSpace());
        assertEquals(building.getUsableSpace(), result.getUsableSpace());
        
        final BuildingEntity entity = entityManager
            .createQuery("SELECT b FROM BuildingEntity b where b.title = :title", BuildingEntity.class)
            .setParameter("title", TestData.BUILDING_TITLE)
            .getSingleResult();
        assertEquals(result, entity);
    }
    
    @Test
    void getBuilding_SUCCESS_buildingRetrieved() {
        assertNotNull(TestData.propertyBuilder().build());
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId());
        final BuildingModel building = buildingController.createBuilding(TestData.PROJECT_ID, property.getId(),
            TestData.buildingBuilder().id(null).address(TestData.addressBuilder().build()).build());
        assertNotNull(building.getId());

        final BuildingModel result = buildingController.getBuilding(TestData.PROJECT_ID, property.getId(), building.getId());
        
        assertEquals(building.getId(), result.getId());
        assertEquals(building.getTitle(), result.getTitle());
        assertEquals(building.getDescription(), result.getDescription());
        assertEquals(building.getLivingSpace(), result.getLivingSpace());
        assertEquals(building.getCommercialSpace(), result.getCommercialSpace());
        assertEquals(building.getUsableSpace(), result.getUsableSpace());
    }
    
    @Test
    void getBuilding_FAILED_wrongProjectId() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID_1, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);
        final String buildingId = buildingController
            .createBuilding(TestData.PROJECT_ID, propertyId,
            TestData.buildingBuilder().id(null).address(TestData.addressBuilder().build()).build())
            .getId();
        assertNotNull(buildingId);
        
        assertThrows(NotFoundException.class,
            () -> buildingController.getBuilding(TestData.PROJECT_ID_2, propertyId, buildingId));
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
        final ApartmentModel result = buildingController
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

        final ApartmentModel getResult = buildingController
            .getApartment(TestData.PROJECT_ID, buildingId, apartmentId);
        
        assertEquals(result, getResult);
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
        final CommercialModel result = buildingController
            .createCommercial(TestData.PROJECT_ID, buildingId, commercial);
        
        assertNotEquals(commercial.getId(), result.getId());
        assertEquals(commercial.getTitle(), result.getTitle());
        assertEquals(commercial.getLocation(), result.getLocation());
        assertEquals(commercial.getDescription(), result.getDescription());
        assertEquals(commercial.getCommercialSpace(), result.getCommercialSpace());
        assertEquals(commercial.getUsableSpace(), result.getUsableSpace());
        
        final String commercialId = entityManager
            .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", String.class)
            .setParameter("title", TestData.COMMERCIAL_TITLE)
            .getSingleResult();
        assertEquals(result.getId(), commercialId);

        final CommercialModel getResult = buildingController
            .getCommercial(TestData.PROJECT_ID, buildingId, commercialId);
        
        assertEquals(result, getResult);
    }

   /* @Test
    void createGarage_SUCCESS_getGarage() {
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
        
        final GarageModel garage = TestData.garageBuilder().build();
        final GarageModel result = buildingController
            .createGarage(TestData.PROJECT_ID, buildingId, garage);
        
        assertNotEquals(garage.getId(), result.getId());
        assertEquals(garage.getTitle(), result.getTitle());
        assertEquals(garage.getLocation(), result.getLocation());
        assertEquals(garage.getDescription(), result.getDescription());
        assertEquals(garage.getUsableSpace(), result.getUsableSpace());
        
        final String garageId = entityManager
            .createQuery("SELECT g.id FROM GarageEntity g where g.title = :title", String.class)
            .setParameter("title", TestData.GARAGE_TITLE)
            .getSingleResult();
        assertEquals(result.getId(), garageId);

        final GarageModel getResult = buildingController
            .getGarage(TestData.PROJECT_ID, buildingId, garageId);
        
        assertEquals(result, getResult);
    }*/

}
