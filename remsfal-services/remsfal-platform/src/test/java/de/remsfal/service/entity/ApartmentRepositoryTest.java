package de.remsfal.service.entity;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.BuildingController;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dto.ApartmentEntity;
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
class ApartmentRepositoryTest extends AbstractServiceTest {

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
                .createNativeQuery("INSERT INTO projects (ID, TITLE) VALUES (?, ?)")
                .setParameter(1, convert(TestData.PROJECT_ID.toString()))
                .setParameter(2, TestData.PROJECT_TITLE)
                .executeUpdate());
        propertyId = propertyController
                .createProperty(TestData.PROJECT_ID.toString(), TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);
        buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID.toString(), propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO apartments (ID, PROJECT_ID, BUILDING_ID, TITLE) VALUES (?, ?, ?, ?)")
                .setParameter(1, convert(TestData.APARTMENT_ID.toString()))
                .setParameter(2, convert(TestData.PROJECT_ID.toString()))
                .setParameter(3, buildingId)
                .setParameter(4, TestData.APARTMENT_TITLE)
                .executeUpdate());
    }

    @Test
    void testFindByIds_and_hashcode() {
        final Optional<ApartmentEntity> found = repository.findByIds(TestData.PROJECT_ID.toString(), TestData.APARTMENT_ID.toString());
        final ApartmentModel apartment = TestData.apartmentBuilder().build();
        assertTrue(found.isPresent());
        assertTrue(found.hashCode() != 0);
        assertEquals(apartment.getId(), found.get().getId());
        assertEquals(apartment.getTitle(), found.get().getTitle());
    }

    @Test
    void testRemoveApartmentByIds() {
        final long removeApartmentByIds = runInTransaction(() ->
                repository.removeApartmentByIds(TestData.PROJECT_ID.toString(), TestData.APARTMENT_ID.toString()));
        assertEquals(1, removeApartmentByIds);
    }
}
