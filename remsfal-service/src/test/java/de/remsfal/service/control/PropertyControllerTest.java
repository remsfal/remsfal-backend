package de.remsfal.service.control;

import de.remsfal.core.json.project.ImmutablePropertyJson;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.NotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.PropertyEntity;

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
        List<? extends PropertyModel> properties = propertyController.getProperties(TestData.PROJECT_ID, 0, 100);
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

}
