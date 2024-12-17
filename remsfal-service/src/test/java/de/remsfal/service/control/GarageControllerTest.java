package de.remsfal.service.control;

import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.GarageEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
class GarageControllerTest extends AbstractTest {

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

    @Inject
    GarageController garageController;

    @BeforeEach
    void setupTestProjects() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.PROJECT_TITLE)
                .executeUpdate());
    }

    @Test
    void createGarage_SUCCESS_idGenerated() {
        // Step 1: Create a Property
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId(), "Property ID should not be null");

        // Step 2: Create a Building linked to the Property
        final BuildingModel building = TestData.buildingBuilder()
                .id(null)
                .address(TestData.addressBuilder().build())
                .build();

        final BuildingModel buildingResult = buildingController.createBuilding(TestData.PROJECT_ID, property.getId(), building);
        assertNotNull(buildingResult.getId(), "Building ID should not be null");

        // Step 3: Create a Garage linked to the Building
        final GarageModel garage = TestData.garageBuilder()
                .id(null)
                .build();

        final GarageModel garageResult = garageController.createGarage(TestData.PROJECT_ID, buildingResult.getId(), garage);

        // Assertions: Check Garage ID and other properties
        assertNotNull(garageResult.getId(), "Garage ID should not be null");
        assertNotEquals(garage.getId(), garageResult.getId());
        assertEquals(garage.getTitle(), garageResult.getTitle());
        assertEquals(garage.getLocation(), garageResult.getLocation());
        assertEquals(garage.getDescription(), garageResult.getDescription());
        assertEquals(garage.getUsableSpace(), garageResult.getUsableSpace());

        // Verify Garage exists in the database
        final GarageEntity entity = entityManager
                .createQuery("SELECT g FROM GarageEntity g WHERE g.title = :title", GarageEntity.class)
                .setParameter("title", TestData.GARAGE_TITLE)
                .getSingleResult();
        assertEquals(garageResult.getId(), entity.getId());
        assertEquals(garageResult.getTitle(), entity.getTitle());
    }

}
