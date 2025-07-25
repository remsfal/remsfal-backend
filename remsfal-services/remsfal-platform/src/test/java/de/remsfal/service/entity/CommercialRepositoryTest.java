package de.remsfal.service.entity;

import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.BuildingController;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dto.CommercialEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wildfly.common.Assert.assertTrue;

@QuarkusTest
class CommercialRepositoryTest extends AbstractServiceTest {

    @Inject
    CommercialRepository repository;

    @Inject
    BuildingController buildingController;

    @Inject
    PropertyController propertyController;

    String propertyId;

    String buildingId;

    @BeforeEach
    public void setupCommercials() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?, ?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.PROJECT_TITLE)
                .executeUpdate());
        propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);
        buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO COMMERCIAL (ID, PROJECT_ID, BUILDING_ID, TITLE) VALUES (?, ?, ?, ?)")
                .setParameter(1, TestData.COMMERCIAL_ID)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, buildingId)
                .setParameter(4, TestData.COMMERCIAL_TITLE)
                .executeUpdate());
    }

    @Test
    void testCommercialById() {
        final Optional<CommercialEntity> found = repository.findCommercialById(TestData.PROJECT_ID, TestData.COMMERCIAL_ID);
        final CommercialModel commercial = TestData.commercialBuilder().build();
        assertTrue(found.isPresent());
        assertTrue(found.hashCode() != 0);
        assertEquals(commercial.getId(), found.get().getId());
        assertEquals(commercial.getTitle(), found.get().getTitle());
    }

    @Test
    void testDeleteCommercialById() {
        final long deleteCommercialById = runInTransaction(() ->
                repository.deleteCommercialById(TestData.PROJECT_ID, TestData.COMMERCIAL_ID));
        assertEquals(1, deleteCommercialById);
    }
}