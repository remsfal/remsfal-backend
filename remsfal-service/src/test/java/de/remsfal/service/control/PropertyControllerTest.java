package de.remsfal.service.control;

import de.remsfal.core.json.project.ImmutablePropertyJson;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.NotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.PropertyEntity;
import de.remsfal.service.entity.dto.SiteEntity;

import java.util.List;

@QuarkusTest
class PropertyControllerTest extends AbstractTest {

    @Inject
    PropertyController propertyController;

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
    void createProperty_FAILED_noProject() {
        final PropertyModel property = TestData.propertyBuilder().build();
        
        assertThrows(ConstraintViolationException.class,
            () -> propertyController.createProperty(null, property));
    }
    
    @Test
    void createProperty_SUCCESS_idGenerated() {
        final PropertyModel property = TestData.propertyBuilder().build();
        
        final PropertyModel result = propertyController.createProperty(TestData.PROJECT_ID, property);
        
        assertNotEquals(property.getId(), result.getId());
        assertEquals(property.getTitle(), result.getTitle());
        assertEquals(property.getLandRegisterEntry(), result.getLandRegisterEntry());
        assertEquals(property.getDescription(), result.getDescription());
        assertEquals(property.getPlotArea(), result.getPlotArea());
        
        final PropertyEntity entity = entityManager
            .createQuery("SELECT p FROM PropertyEntity p where p.title = :title", PropertyEntity.class)
            .setParameter("title", TestData.PROPERTY_TITLE)
            .getSingleResult();
        assertEquals(result, entity);
    }

    @Test
    void deleteProperty_SUCCESS_correctlyDeleted() {
        // Arrange
        final PropertyModel property = TestData.propertyBuilder().build();
        final PropertyModel createdProperty = propertyController.createProperty(TestData.PROJECT_ID, property);
        String propertyId = createdProperty.getId();
        // Act
        boolean deleted = propertyController.deleteProperty(TestData.PROJECT_ID, propertyId);
        // Assert
        assertTrue(deleted);
        assertThrows(NoResultException.class, () -> findPropertyById(propertyId));
    }

    private PropertyEntity findPropertyById(String propertyId) {
        return entityManager
            .createQuery("SELECT p FROM PropertyEntity p where p.id = :id", PropertyEntity.class)
            .setParameter("id", propertyId)
            .getSingleResult();
    }

    @Test
    void deleteProperty_FAILED_notDeleted() {
        // Arrange
        String notExistingPropertyId = "bfbada15-d3d5-4925-a438-260821532b54";
        // Act
        boolean deleted = propertyController.deleteProperty(TestData.PROJECT_ID, notExistingPropertyId);
        // Assert
        assertFalse(deleted);
    }

    @Test
    void updateProperty_SUCCESS_correctlyUpdated() {
        // Arrange
        final PropertyModel property = TestData.propertyBuilder().build();
        final PropertyModel createdProperty = propertyController.createProperty(TestData.PROJECT_ID, property);
        // Act
        PropertyModel newPropertyValues = ImmutablePropertyJson.builder()
            .title(TestData.PROPERTY_ID_2)
            .landRegisterEntry(TestData.PROPERTY_REG_ENTRY_2)
            .description(TestData.PROPERTY_DESCRIPTION_2)
            .plotArea(TestData.PROPERTY_PLOT_AREA_2)
            .build();
        PropertyModel updatedProperty = propertyController.updateProperty(TestData.PROJECT_ID, createdProperty.getId(), newPropertyValues);
        // Assert
        PropertyModel updatedPropertyFromDb = entityManager
            .createQuery("SELECT p FROM PropertyEntity p where p.id = :id", PropertyEntity.class)
            .setParameter("id", updatedProperty.getId())
            .getSingleResult();
        assertEquals(updatedProperty.getId(), updatedPropertyFromDb.getId());
        assertProperty(newPropertyValues, updatedPropertyFromDb);
    }

    @Test
    void updateProperty_FAILED_propertyNotFound() {
        // Arrange
        String notExistingPropertyId = "bfbada15-d3d5-4925-a438-260821532b54";
        // act + Assert
        PropertyModel newPropertyValues = ImmutablePropertyJson.builder()
            .title("new title")
            .landRegisterEntry("new register entry")
            .description("new description")
            .plotArea(999)
            .build();
        assertThrows(NotFoundException.class,
            () -> propertyController.updateProperty(TestData.PROJECT_ID, notExistingPropertyId, newPropertyValues));
    }

    @Test
    void getProperties_SUCCESS_correctlyReturned() {
        // Arrange
        final PropertyModel property1 = ImmutablePropertyJson.builder()
            .title("Property 1")
            .landRegisterEntry("register entry 1")
            .description("description 1")
            .plotArea(111)
            .build();
        final PropertyModel property2 = ImmutablePropertyJson.builder()
            .title("Property 2")
            .landRegisterEntry("register entry 2")
            .description("description 2")
            .plotArea(999)
            .build();

        final PropertyModel createdProperty1 = propertyController.createProperty(TestData.PROJECT_ID, property1);
        final PropertyModel createdProperty2 = propertyController.createProperty(TestData.PROJECT_ID, property2);
        // Act
        List<PropertyModel> properties = propertyController.getProperties(TestData.PROJECT_ID, 0, 100);
        // Assert
        assertEquals(2, properties.size());
        assertProperty(property1, createdProperty1);
        assertProperty(property2, createdProperty2);
    }

    private void assertProperty(PropertyModel expectedProperty, PropertyModel actualProperty) {
        assertEquals(expectedProperty.getTitle(), actualProperty.getTitle());
        assertEquals(expectedProperty.getLandRegisterEntry(), actualProperty.getLandRegisterEntry());
        assertEquals(expectedProperty.getDescription(), actualProperty.getDescription());
        assertEquals(expectedProperty.getPlotArea(), actualProperty.getPlotArea());
    }

    @Test
    void getProperty_SUCCESS_propertyRetrieved() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION)
            .setParameter(6, 22)
            .executeUpdate());
        
        final PropertyModel result = propertyController.getProperty(TestData.PROJECT_ID, TestData.PROPERTY_ID);
        
        assertEquals(TestData.PROPERTY_ID, result.getId());
        assertEquals(TestData.PROPERTY_TITLE, result.getTitle());
        assertEquals(TestData.PROPERTY_REG_ENTRY, result.getLandRegisterEntry());
        assertEquals(TestData.PROPERTY_DESCRIPTION, result.getDescription());
        assertEquals(22, result.getPlotArea());
    }
    
    @Test
    void getProperty_FAILED_wrongProjectId() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID)
            .setParameter(2, TestData.PROJECT_ID_1)
            .setParameter(3, TestData.PROPERTY_TITLE)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION)
            .setParameter(6, 22)
            .executeUpdate());
        
        assertThrows(NotFoundException.class,
            () -> propertyController.getProperty(TestData.PROJECT_ID_2, TestData.PROPERTY_ID));
    }
    
    @Test
    void createSite_FAILED_noProjectNoProperty() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);

        final SiteModel site = TestData.siteBuilder()
            	.id(null)
                .address(TestData.addressBuilder().build())
                .build();
        
        assertThrows(ConstraintViolationException.class,
            () -> propertyController.createSite(null, propertyId, site));
        assertThrows(ConstraintViolationException.class,
            () -> propertyController.createSite(TestData.PROJECT_ID, null, site));
    }
    
    @Test
    void createSite_SUCCESS_idGenerated() {
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId());

        final SiteModel site = TestData.siteBuilder()
        	.id(null)
            .address(TestData.addressBuilder().build())
            .build();
        
        final SiteModel result = propertyController.createSite(TestData.PROJECT_ID, property.getId(), site);
        
        assertNotEquals(site.getId(), result.getId());
        assertEquals(site.getTitle(), result.getTitle());
        assertEquals(site.getAddress().getStreet(), result.getAddress().getStreet());
        assertEquals(site.getAddress().getCity(), result.getAddress().getCity());
        assertEquals(site.getAddress().getProvince(), result.getAddress().getProvince());
        assertEquals(site.getAddress().getZip(), result.getAddress().getZip());
        assertEquals(site.getDescription(), result.getDescription());
        assertEquals(site.getUsableSpace(), result.getUsableSpace());
        assertEquals(site.getRent(), result.getRent());
        
        final SiteEntity entity = entityManager
            .createQuery("SELECT s FROM SiteEntity s where s.title = :title", SiteEntity.class)
            .setParameter("title", TestData.SITE_TITLE)
            .getSingleResult();
        assertEquals(result, entity);
    }
    
    @Test
    void getSite_SUCCESS_siteRetrieved() {
        assertNotNull(TestData.propertyBuilder().build());
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId());
        final SiteModel site = propertyController.createSite(TestData.PROJECT_ID, property.getId(),
            TestData.siteBuilder().id(null).address(TestData.addressBuilder().build()).build());
        assertNotNull(site.getId());

        final SiteModel result = propertyController.getSite(TestData.PROJECT_ID, property.getId(), site.getId());
        
        assertEquals(site.getId(), result.getId());
        assertEquals(site.getTitle(), result.getTitle());
        assertEquals(site.getDescription(), result.getDescription());
        assertEquals(site.getUsableSpace(), result.getUsableSpace());
        assertEquals(site.getRent(), result.getRent());
    }
    
    @Test
    void getSite_FAILED_wrongProjectId() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID_1, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);
        final String siteId = propertyController
            .createSite(TestData.PROJECT_ID, propertyId,
            TestData.siteBuilder().id(null).address(TestData.addressBuilder().build()).build())
            .getId();
        assertNotNull(siteId);
        
        assertThrows(NotFoundException.class,
            () -> propertyController.getSite(TestData.PROJECT_ID_2, propertyId, siteId));
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
            () -> propertyController.createBuilding(null, buildingId, building));
        assertThrows(ConstraintViolationException.class,
            () -> propertyController.createBuilding(TestData.PROJECT_ID, null, building));
    }
    
    @Test
    void createBuilding_SUCCESS_idGenerated() {
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId());

        final BuildingModel building = TestData.buildingBuilder()
        	.id(null)
            .address(TestData.addressBuilder().build())
            .build();
        
        final BuildingModel result = propertyController.createBuilding(TestData.PROJECT_ID, property.getId(), building);
        
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
        assertEquals(building.getRent(), result.getRent());
        
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
        final BuildingModel building = propertyController.createBuilding(TestData.PROJECT_ID, property.getId(),
            TestData.buildingBuilder().id(null).address(TestData.addressBuilder().build()).build());
        assertNotNull(building.getId());

        final BuildingModel result = propertyController.getBuilding(TestData.PROJECT_ID, property.getId(), building.getId());
        
        assertEquals(building.getId(), result.getId());
        assertEquals(building.getTitle(), result.getTitle());
        assertEquals(building.getDescription(), result.getDescription());
        assertEquals(building.getLivingSpace(), result.getLivingSpace());
        assertEquals(building.getCommercialSpace(), result.getCommercialSpace());
        assertEquals(building.getUsableSpace(), result.getUsableSpace());
        assertEquals(building.getRent(), result.getRent());
    }
    
    @Test
    void getBuilding_FAILED_wrongProjectId() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID_1, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);
        final String buildingId = propertyController
            .createBuilding(TestData.PROJECT_ID, propertyId,
            TestData.buildingBuilder().id(null).address(TestData.addressBuilder().build()).build())
            .getId();
        assertNotNull(buildingId);
        
        assertThrows(NotFoundException.class,
            () -> propertyController.getBuilding(TestData.PROJECT_ID_2, propertyId, buildingId));
    }
    
    @Test
    void createApartment_SUCCESS_getApartment() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);

        final String buildingId = propertyController
            .createBuilding(TestData.PROJECT_ID, propertyId,
                TestData.buildingBuilder()
                .id(null)
                .address(TestData.addressBuilder().build())
                .build())
            .getId();
        assertNotNull(buildingId);
        
        final ApartmentModel apartment = TestData.apartmentBuilder().build();
        final ApartmentModel result = propertyController
            .createApartment(TestData.PROJECT_ID, buildingId, apartment);
        
        assertNotEquals(apartment.getId(), result.getId());
        assertEquals(apartment.getTitle(), result.getTitle());
        assertEquals(apartment.getLocation(), result.getLocation());
        assertEquals(apartment.getDescription(), result.getDescription());
        assertEquals(apartment.getLivingSpace(), result.getLivingSpace());
        assertEquals(apartment.getUsableSpace(), result.getUsableSpace());
        assertEquals(apartment.getRent(), result.getRent());
        
        final String apartmentId = entityManager
            .createQuery("SELECT a.id FROM ApartmentEntity a where a.title = :title", String.class)
            .setParameter("title", TestData.APARTMENT_TITLE)
            .getSingleResult();
        assertEquals(result.getId(), apartmentId);

        final ApartmentModel getResult = propertyController
            .getApartment(TestData.PROJECT_ID, buildingId, apartmentId);
        
        assertEquals(result, getResult);
    }

    @Test
    void createCommercial_SUCCESS_getCommercial() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);

        final String buildingId = propertyController
            .createBuilding(TestData.PROJECT_ID, propertyId,
                TestData.buildingBuilder()
                .id(null)
                .address(TestData.addressBuilder().build())
                .build())
            .getId();
        assertNotNull(buildingId);
        
        final CommercialModel commercial = TestData.commercialBuilder().build();
        final CommercialModel result = propertyController
            .createCommercial(TestData.PROJECT_ID, buildingId, commercial);
        
        assertNotEquals(commercial.getId(), result.getId());
        assertEquals(commercial.getTitle(), result.getTitle());
        assertEquals(commercial.getLocation(), result.getLocation());
        assertEquals(commercial.getDescription(), result.getDescription());
        assertEquals(commercial.getCommercialSpace(), result.getCommercialSpace());
        assertEquals(commercial.getUsableSpace(), result.getUsableSpace());
        assertEquals(commercial.getRent(), result.getRent());
        
        final String commercialId = entityManager
            .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", String.class)
            .setParameter("title", TestData.COMMERCIAL_TITLE)
            .getSingleResult();
        assertEquals(result.getId(), commercialId);

        final CommercialModel getResult = propertyController
            .getCommercial(TestData.PROJECT_ID, buildingId, commercialId);
        
        assertEquals(result, getResult);
    }

    @Test
    void createGarage_SUCCESS_getGarage() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);

        final String buildingId = propertyController
            .createBuilding(TestData.PROJECT_ID, propertyId,
                TestData.buildingBuilder()
                .id(null)
                .address(TestData.addressBuilder().build())
                .build())
            .getId();
        assertNotNull(buildingId);
        
        final GarageModel garage = TestData.garageBuilder().build();
        final GarageModel result = propertyController
            .createGarage(TestData.PROJECT_ID, buildingId, garage);
        
        assertNotEquals(garage.getId(), result.getId());
        assertEquals(garage.getTitle(), result.getTitle());
        assertEquals(garage.getLocation(), result.getLocation());
        assertEquals(garage.getDescription(), result.getDescription());
        assertEquals(garage.getUsableSpace(), result.getUsableSpace());
        assertEquals(garage.getRent(), result.getRent());
        
        final String garageId = entityManager
            .createQuery("SELECT g.id FROM GarageEntity g where g.title = :title", String.class)
            .setParameter("title", TestData.GARAGE_TITLE)
            .getSingleResult();
        assertEquals(result.getId(), garageId);

        final GarageModel getResult = propertyController
            .getGarage(TestData.PROJECT_ID, buildingId, garageId);
        
        assertEquals(result, getResult);
    }

}
