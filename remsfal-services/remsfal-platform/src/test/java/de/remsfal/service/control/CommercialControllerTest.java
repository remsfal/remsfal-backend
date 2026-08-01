package de.remsfal.service.control;

import de.remsfal.core.json.project.ImmutableCommercialJson;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.test.TestData;

import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CommercialControllerTest extends AbstractServiceTest {

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

    @Inject
    CommercialController commercialController;

    @BeforeEach
    void setup() {
        super.setupTestUsers();
        super.setupTestProjects();
    }

    private UUID createBuildingForCommercial() {
        final UUID propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        return buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
    }

    @Test
    void createCommercial_SUCCESS_getCommercial() {
        final UUID propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final UUID buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);

        final CommercialModel commercial = TestData.commercialBuilder().build();
        final CommercialModel result = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId, commercial);

        assertNotEquals(commercial.getId(), result.getId());
        assertEquals(commercial.getTitle(), result.getTitle());
        assertEquals(commercial.getLocation(), result.getLocation());
        assertEquals(commercial.getNetFloorArea(), result.getNetFloorArea());
        assertEquals(commercial.getHeatingSpace(), result.getHeatingSpace());

        final UUID commercialId = entityManager
                .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", UUID.class)
                .setParameter("title", TestData.COMMERCIAL_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), commercialId);

        final CommercialModel getResult = commercialController
                .getCommercial(TestData.PROJECT_ID, commercialId);

        assertEquals(result, getResult);
    }

    @Test
    void createCommercial_SUCCESS_deleteCommercial() {
        final UUID propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final UUID buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);

        final CommercialModel commercial = TestData.commercialBuilder().build();
        final CommercialModel result = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId, commercial);

        final UUID commercialId = entityManager
                .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", UUID.class)
                .setParameter("title", TestData.COMMERCIAL_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), commercialId);

        commercialController.deleteCommercial(TestData.PROJECT_ID, commercialId);
        assertThrows(NotFoundException.class, () -> commercialController.getCommercial(TestData.PROJECT_ID, commercialId));
    }

    @Test
    void createCommercial_SUCCESS_updateCommercial() {
        final UUID propertyId = propertyController
                .createProperty(TestData.PROJECT_ID, TestData.propertyBuilder().build())
                .getId();
        assertNotNull(propertyId);

        final UUID buildingId = buildingController
                .createBuilding(TestData.PROJECT_ID, propertyId,
                        TestData.buildingBuilder()
                                .id(null)
                                .address(TestData.addressBuilder().build())
                                .build())
                .getId();
        assertNotNull(buildingId);

        final CommercialModel commercial = TestData.commercialBuilder().build();
        final CommercialModel result = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId, commercial);

        assertNotNull(result.getId());
        assertEquals(commercial.getTitle(), result.getTitle());
        assertEquals(commercial.getLocation(), result.getLocation());
        assertEquals(commercial.getNetFloorArea(), result.getNetFloorArea());
        assertEquals(commercial.getUsableFloorArea(), result.getUsableFloorArea());
        assertEquals(commercial.getTechnicalServicesArea(), result.getTechnicalServicesArea());
        assertEquals(commercial.getTrafficArea(), result.getTrafficArea());
        assertEquals(commercial.getHeatingSpace(), result.getHeatingSpace());

        final UUID commercialId = entityManager
                .createQuery("SELECT c.id FROM CommercialEntity c where c.title = :title", UUID.class)
                .setParameter("title", TestData.COMMERCIAL_TITLE)
                .getSingleResult();
        assertEquals(result.getId(), commercialId);
    }

    @Test
    void updateCommercial_SUCCESS_setsPositiveAreas() {
        final UUID buildingId = createBuildingForCommercial();

        final CommercialModel created = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId, TestData.commercialBuilder().build());

        final CommercialModel update = ImmutableCommercialJson.builder()
                .usableFloorArea(TestData.COMMERCIAL_USABLE_FLOOR_AREA_2)
                .technicalServicesArea(TestData.COMMERCIAL_TECHNICAL_SERVICE_AREA_2)
                .trafficArea(TestData.COMMERCIAL_TRAFFIC_AREA_2)
                .build();

        final CommercialModel result = commercialController
                .updateCommercial(TestData.PROJECT_ID, created.getId(), update);

        assertEquals(TestData.COMMERCIAL_USABLE_FLOOR_AREA_2, result.getUsableFloorArea());
        assertEquals(TestData.COMMERCIAL_TECHNICAL_SERVICE_AREA_2, result.getTechnicalServicesArea());
        assertEquals(TestData.COMMERCIAL_TRAFFIC_AREA_2, result.getTrafficArea());
    }

    @Test
    void updateCommercial_SUCCESS_zeroAreasIgnoredWithoutNetFloorArea() {
        final UUID buildingId = createBuildingForCommercial();

        final CommercialModel created = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId,
                        TestData.commercialBuilder2().build());

        final CommercialModel update = ImmutableCommercialJson.builder()
                .usableFloorArea(0f)
                .technicalServicesArea(0f)
                .trafficArea(0f)
                .build();

        final CommercialModel result = commercialController
                .updateCommercial(TestData.PROJECT_ID, created.getId(), update);

        assertEquals(TestData.COMMERCIAL_USABLE_FLOOR_AREA_2, result.getUsableFloorArea());
        assertEquals(TestData.COMMERCIAL_TECHNICAL_SERVICE_AREA_2, result.getTechnicalServicesArea());
        assertEquals(TestData.COMMERCIAL_TRAFFIC_AREA_2, result.getTrafficArea());
    }

    @Test
    void updateCommercial_SUCCESS_netFloorAreaClearsZeroedAreas() {
        final UUID buildingId = createBuildingForCommercial();

        final CommercialModel created = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId,
                        TestData.commercialBuilder2().build());
        assertEquals(TestData.COMMERCIAL_USABLE_FLOOR_AREA_2, created.getUsableFloorArea());

        final CommercialModel update = ImmutableCommercialJson.builder()
                .netFloorArea(TestData.COMMERCIAL_NET_FLOOR_AREA_1)
                .usableFloorArea(0f)
                .technicalServicesArea(0f)
                .trafficArea(0f)
                .build();

        final CommercialModel result = commercialController
                .updateCommercial(TestData.PROJECT_ID, created.getId(), update);

        assertEquals(TestData.COMMERCIAL_NET_FLOOR_AREA_1, result.getNetFloorArea());
        assertNull(result.getUsableFloorArea());
        assertNull(result.getTechnicalServicesArea());
        assertNull(result.getTrafficArea());
    }

    @Test
    void updateCommercial_SUCCESS_netFloorAreaKeepsAreasWhenNotAllZero() {
        final UUID buildingId = createBuildingForCommercial();

        final CommercialModel created = commercialController
                .createCommercial(TestData.PROJECT_ID, buildingId,
                        TestData.commercialBuilder2().build());

        final CommercialModel update = ImmutableCommercialJson.builder()
                .netFloorArea(TestData.COMMERCIAL_NET_FLOOR_AREA_1)
                .usableFloorArea(0f)
                .technicalServicesArea(TestData.COMMERCIAL_TECHNICAL_SERVICE_AREA_2)
                .trafficArea(0f)
                .build();

        final CommercialModel result = commercialController
                .updateCommercial(TestData.PROJECT_ID, created.getId(), update);

        assertEquals(TestData.COMMERCIAL_NET_FLOOR_AREA_1, result.getNetFloorArea());
        assertEquals(TestData.COMMERCIAL_USABLE_FLOOR_AREA_2, result.getUsableFloorArea());
        assertEquals(TestData.COMMERCIAL_TECHNICAL_SERVICE_AREA_2, result.getTechnicalServicesArea());
        assertEquals(TestData.COMMERCIAL_TRAFFIC_AREA_2, result.getTrafficArea());
    }

}
