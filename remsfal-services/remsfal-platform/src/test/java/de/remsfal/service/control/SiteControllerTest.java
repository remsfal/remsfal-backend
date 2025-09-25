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

import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dto.SiteEntity;
import de.remsfal.test.TestData;

@QuarkusTest
class SiteControllerTest extends AbstractServiceTest {

    @Inject
    PropertyController propertyController;

    @Inject
    SiteController siteController;

    @BeforeEach
    void setupTestProjects() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.PROJECT_TITLE_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_2)
            .setParameter(2, TestData.PROJECT_TITLE_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_3)
            .setParameter(2, TestData.PROJECT_TITLE_3)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_4)
            .setParameter(2, TestData.PROJECT_TITLE_4)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_5)
            .setParameter(2, TestData.PROJECT_TITLE_5)
            .executeUpdate());
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
            () -> siteController.createSite(null, propertyId, site));
        assertThrows(ConstraintViolationException.class,
            () -> siteController.createSite(TestData.PROJECT_ID, null, site));
    }
    
    @Test
    void createSite_SUCCESS_idGenerated() {
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId());

        final SiteModel site = TestData.siteBuilder()
        	.id(null)
            .address(TestData.addressBuilder().build())
            .build();
        
        final SiteModel result = siteController.createSite(TestData.PROJECT_ID, property.getId(), site);
        
        assertNotEquals(site.getId(), result.getId());
        assertEquals(site.getTitle(), result.getTitle());
        assertEquals(site.getAddress().getStreet(), result.getAddress().getStreet());
        assertEquals(site.getAddress().getCity(), result.getAddress().getCity());
        assertEquals(site.getAddress().getProvince(), result.getAddress().getProvince());
        assertEquals(site.getAddress().getZip(), result.getAddress().getZip());
        assertEquals(site.getDescription(), result.getDescription());
        assertEquals(site.getOutdoorArea(), result.getOutdoorArea());
        
        final SiteEntity entity = entityManager
            .createQuery("SELECT s FROM SiteEntity s where s.title = :title", SiteEntity.class)
            .setParameter("title", TestData.SITE_TITLE)
            .getSingleResult();
        assertEquals(entity.hashCode(), result.hashCode());
        assertEquals(entity, result);
    }
    
    @Test
    void getSite_SUCCESS_siteRetrieved() {
        assertNotNull(TestData.propertyBuilder().build());
        final PropertyModel property = propertyController.createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build());
        assertNotNull(property.getId());
        final SiteModel site = siteController.createSite(TestData.PROJECT_ID, property.getId(),
            TestData.siteBuilder().id(null).address(TestData.addressBuilder().build()).build());
        assertNotNull(site.getId());

        final SiteModel result = siteController.getSite(TestData.PROJECT_ID, site.getId());
        
        assertEquals(site.getId(), result.getId());
        assertEquals(site.getTitle(), result.getTitle());
        assertEquals(site.getDescription(), result.getDescription());
        assertEquals(site.getOutdoorArea(), result.getOutdoorArea());
    }
    
    @Test
    void getSite_FAILED_wrongProjectId() {
        final String propertyId = propertyController
            .createProperty(TestData.PROJECT_ID_1, TestData.propertyBuilder().build())
            .getId();
        assertNotNull(propertyId);
        final String siteId = siteController
            .createSite(TestData.PROJECT_ID, propertyId,
            TestData.siteBuilder().id(null).address(TestData.addressBuilder().build()).build())
            .getId();
        assertNotNull(siteId);
        
        assertThrows(NotFoundException.class,
            () -> siteController.getSite(TestData.PROJECT_ID_2, siteId));
    }
    
}
