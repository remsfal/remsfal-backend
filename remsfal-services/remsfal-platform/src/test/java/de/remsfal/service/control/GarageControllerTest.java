package de.remsfal.service.control;

import de.remsfal.core.json.project.StorageJson;
import de.remsfal.core.json.project.ImmutableStorageJson;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.core.model.project.StorageModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.StorageEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
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

    private PropertyModel createTestProperty() {
        PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId(), "Property ID should not be null");
        return property;
    }

    private BuildingModel createTestBuilding(PropertyModel property) {
        BuildingModel building = TestData.buildingBuilder()
                .id(null)
                .address(TestData.addressBuilder().build())
                .build();

        BuildingModel buildingResult = buildingController.createBuilding(TestData.PROJECT_ID, property.getId(), building);
        assertNotNull(buildingResult.getId(), "Building ID should not be null");
        return buildingResult;
    }

    @Test
    void createGarage_SUCCESS_idGenerated() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult =createTestBuilding(property);

        final StorageModel garage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel garageResult = garageController.createGarage(TestData.PROJECT_ID, buildingResult.getId(), garage);

        assertNotNull(garageResult.getId(), "Garage ID should not be null");
        assertNotEquals(garage.getId(), garageResult.getId());
        assertEquals(garage.getTitle(), garageResult.getTitle());
        assertEquals(garage.getLocation(), garageResult.getLocation());
        assertEquals(garage.getDescription(), garageResult.getDescription());
        assertEquals(garage.getUsableSpace(), garageResult.getUsableSpace());

        final StorageEntity entity = entityManager
                .createQuery("SELECT g FROM GarageEntity g WHERE g.title = :title", StorageEntity.class)
                .setParameter("title", TestData.STORAGE_TITLE)
                .getSingleResult();
        assertEquals(garageResult.getId(), entity.getId());
        assertEquals(garageResult.getTitle(), entity.getTitle());
    }

    @Test
    void getGarage_SUCCESS_garageRetrieved() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult = createTestBuilding(property);

        final StorageModel garage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel garageResult = garageController.createGarage(TestData.PROJECT_ID, buildingResult.getId(), garage);
        assertNotNull(garageResult.getId(), "Garage ID should not be null");

        final StorageModel result = garageController.getGarage(TestData.PROJECT_ID, garageResult.getId());

        assertEquals(garageResult.getId(), result.getId(), "Garage ID should match");
        assertEquals(garageResult.getTitle(), result.getTitle(), "Garage title should match");
        assertEquals(garageResult.getLocation(), result.getLocation(), "Garage location should match");
        assertEquals(garageResult.getDescription(), result.getDescription(), "Garage description should match");
        assertEquals(garageResult.getUsableSpace(), result.getUsableSpace(), "Garage usable space should match");
    }

    @Test
    void updateGarage_SUCCESS() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult = createTestBuilding(property);

        final StorageModel garage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel garageResult = garageController.createGarage(TestData.PROJECT_ID, buildingResult.getId(), garage);
        assertNotNull(garageResult.getId(), "Garage ID should not be null");

        StorageModel garageModel = ImmutableStorageJson.builder()
                .id(garageResult.getId())
                .title("Updated Garage Title")
                .location("Updated Location")
                .description("Updated Garage Description")
                .usableSpace(350.0f)
                .build();

        StorageJson updatedGarageJson = StorageJson.valueOf(garageModel);

        final StorageModel updatedGarage = garageController.updateGarage(
                TestData.PROJECT_ID, garageResult.getId(), updatedGarageJson);

        assertEquals(garageResult.getId(), updatedGarage.getId(), "Garage ID should remain the same");
        assertEquals(updatedGarageJson.getTitle(), updatedGarage.getTitle(), "Garage title should be updated");
        assertEquals(updatedGarageJson.getLocation(), updatedGarage.getLocation(), "Garage location should be updated");
        assertEquals(updatedGarageJson.getDescription(), updatedGarage.getDescription(), "Garage description should be updated");
        assertEquals(updatedGarageJson.getUsableSpace(), updatedGarage.getUsableSpace(), "Garage usable space should be updated");
    }

    @Test
    void deleteGarage_SUCCESS() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult = createTestBuilding(property);

        final StorageModel garage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel garageResult = garageController.createGarage(TestData.PROJECT_ID, buildingResult.getId(), garage);
        assertNotNull(garageResult.getId(), "Garage ID should not be null");

        garageController.deleteGarage(TestData.PROJECT_ID, garageResult.getId());

        assertThrows(NotFoundException.class,
                () -> garageController.getGarage(TestData.PROJECT_ID, garageResult.getId()));
    }

}
