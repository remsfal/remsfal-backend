package de.remsfal.service.entity;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.control.BuildingController;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dto.ApartmentEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wildfly.common.Assert.assertTrue;

@QuarkusTest
class ApartmentRepositoryTest extends AbstractTest {

    @Inject
    ApartmentRepository repository;

    @Inject
    BuildingController buildingController;

    @Inject
    PropertyController propertyController;

    String propertyId;

    String buildingId;

    @BeforeEach
    public void setupApartments() {
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
                .createNativeQuery("INSERT INTO APARTMENT (ID, PROJECT_ID, BUILDING_ID, TITLE) VALUES (?, ?, ?, ?)")
                .setParameter(1, TestData.APARTMENT_ID)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, buildingId)
                .setParameter(4, TestData.APARTMENT_TITLE)
                .executeUpdate());
    }

    @Test
    void testFindByIds_and_hashcode() {
        final Optional<ApartmentEntity> found = repository.findByIds(TestData.PROJECT_ID, TestData.APARTMENT_ID);
        final ApartmentModel apartment = TestData.apartmentBuilder().build();
        assertTrue(found.isPresent());
        assertTrue(found.hashCode() != 0);
        assertEquals(apartment.getId(), found.get().getId());
        assertEquals(apartment.getTitle(), found.get().getTitle());
    }

    @Test
    void testRemoveApartmentByIds() {
        final long removeApartmentByIds = runInTransaction(() ->
                repository.removeApartmentByIds(TestData.PROJECT_ID, TestData.APARTMENT_ID));
        assertEquals(1, removeApartmentByIds);
    }
}
