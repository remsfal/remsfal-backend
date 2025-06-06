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
class storageControllerTest extends AbstractTest {

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

    @Inject
    StorageController storageController;

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
    void createStorage_SUCCESS_idGenerated() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult =createTestBuilding(property);

        final StorageModel Storage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel StorageResult = storageController.createStorage(TestData.PROJECT_ID, buildingResult.getId(), Storage);

        assertNotNull(StorageResult.getId(), "Storage ID should not be null");
        assertNotEquals(Storage.getId(), StorageResult.getId());
        assertEquals(Storage.getTitle(), StorageResult.getTitle());
        assertEquals(Storage.getLocation(), StorageResult.getLocation());
        assertEquals(Storage.getDescription(), StorageResult.getDescription());
        assertEquals(Storage.getUsableSpace(), StorageResult.getUsableSpace());

        final StorageEntity entity = entityManager
                .createQuery("SELECT s FROM StorageEntity s WHERE s.title = :title", StorageEntity.class)
                .setParameter("title", TestData.STORAGE_TITLE)
                .getSingleResult();
        assertEquals(StorageResult.getId(), entity.getId());
        assertEquals(StorageResult.getTitle(), entity.getTitle());
    }

    @Test
    void getStorage_SUCCESS_StorageRetrieved() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult = createTestBuilding(property);

        final StorageModel Storage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel StorageResult = storageController.createStorage(TestData.PROJECT_ID, buildingResult.getId(), Storage);
        assertNotNull(StorageResult.getId(), "Storage ID should not be null");

        final StorageModel result = storageController.getStorage(TestData.PROJECT_ID, StorageResult.getId());

        assertEquals(StorageResult.getId(), result.getId(), "Storage ID should match");
        assertEquals(StorageResult.getTitle(), result.getTitle(), "Storage title should match");
        assertEquals(StorageResult.getLocation(), result.getLocation(), "Storage location should match");
        assertEquals(StorageResult.getDescription(), result.getDescription(), "Storage description should match");
        assertEquals(StorageResult.getUsableSpace(), result.getUsableSpace(), "Storage usable space should match");
    }

    @Test
    void updateStorage_SUCCESS() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult = createTestBuilding(property);

        final StorageModel Storage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel StorageResult = storageController.createStorage(TestData.PROJECT_ID, buildingResult.getId(), Storage);
        assertNotNull(StorageResult.getId(), "Storage ID should not be null");

        StorageModel StorageModel = ImmutableStorageJson.builder()
                .id(StorageResult.getId())
                .title("Updated Storage Title")
                .location("Updated Location")
                .description("Updated Storage Description")
                .usableSpace(350.0f)
                .build();

        StorageJson updatedStorageJson = StorageJson.valueOf(StorageModel);

        final StorageModel updatedStorage = storageController.updateStorage(
                TestData.PROJECT_ID, StorageResult.getId(), updatedStorageJson);

        assertEquals(StorageResult.getId(), updatedStorage.getId(), "Storage ID should remain the same");
        assertEquals(updatedStorageJson.getTitle(), updatedStorage.getTitle(), "Storage title should be updated");
        assertEquals(updatedStorageJson.getLocation(), updatedStorage.getLocation(), "Storage location should be updated");
        assertEquals(updatedStorageJson.getDescription(), updatedStorage.getDescription(), "Storage description should be updated");
        assertEquals(updatedStorageJson.getUsableSpace(), updatedStorage.getUsableSpace(), "Storage usable space should be updated");
    }

    @Test
    void deleteStorage_SUCCESS() {
        final PropertyModel property = createTestProperty();
        final BuildingModel buildingResult = createTestBuilding(property);

        final StorageModel Storage = TestData.storageBuilder()
                .id(null)
                .build();

        final StorageModel StorageResult = storageController.createStorage(TestData.PROJECT_ID, buildingResult.getId(), Storage);
        assertNotNull(StorageResult.getId(), "Storage ID should not be null");

        storageController.deleteStorage(TestData.PROJECT_ID, StorageResult.getId());

        assertThrows(NotFoundException.class,
                () -> storageController.getStorage(TestData.PROJECT_ID, StorageResult.getId()));
    }

}
