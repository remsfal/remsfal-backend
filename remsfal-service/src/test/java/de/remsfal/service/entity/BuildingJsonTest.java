package de.remsfal.service.entity;

import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class BuildingJsonTest extends AbstractTest {

    @Test
    void testValueOf_withNullModel() {
        assertNull(BuildingJson.valueOf(null));
    }

    @Test
    void testValueOf_withValidModel() {
        BuildingModel buildingMock = mock(BuildingModel.class);
        when(buildingMock.getId()).thenReturn(TestData.BUILDING_ID_1);
        when(buildingMock.getTitle()).thenReturn(TestData.PROJECT_TITLE_1);
        when(buildingMock.getAddress()).thenReturn(mock(AddressModel.class));
        when(buildingMock.getDescription()).thenReturn(null);
        when(buildingMock.getLivingSpace()).thenReturn(null);
        when(buildingMock.getCommercialSpace()).thenReturn(null);
        when(buildingMock.getUsableSpace()).thenReturn(null);
        when(buildingMock.getHeatingSpace()).thenReturn(null);

        BuildingJson buildingJson = BuildingJson.valueOf(buildingMock);

        assertNotNull(buildingJson);
        assertEquals(TestData.BUILDING_ID_1, buildingJson.getId());
        assertEquals(TestData.PROJECT_TITLE_1, buildingJson.getTitle());
        assertNotNull(buildingJson.getAddress());
        assertEquals("", buildingJson.getDescription());
        assertEquals(0.0F, buildingJson.getLivingSpace());
        assertEquals(0.0F, buildingJson.getCommercialSpace());
        assertEquals(0.0F, buildingJson.getUsableSpace());
        assertEquals(0.0F, buildingJson.getHeatingSpace());
    }

    @Test
    void testValueOf_withPartiallyFilledModel() {
        BuildingModel buildingMock = mock(BuildingModel.class);
        when(buildingMock.getId()).thenReturn(TestData.BUILDING_ID_1);
        when(buildingMock.getTitle()).thenReturn(TestData.PROJECT_TITLE_1);
        when(buildingMock.getAddress()).thenReturn(mock(AddressModel.class));
        when(buildingMock.getDescription()).thenReturn(null);
        when(buildingMock.getLivingSpace()).thenReturn(50F);
        when(buildingMock.getCommercialSpace()).thenReturn(null);
        when(buildingMock.getUsableSpace()).thenReturn(25F);
        when(buildingMock.getHeatingSpace()).thenReturn(null);

        BuildingJson buildingJson = BuildingJson.valueOf(buildingMock);

        assertNotNull(buildingJson);
        assertEquals(TestData.BUILDING_ID_1, buildingJson.getId());
        assertEquals(TestData.PROJECT_TITLE_1, buildingJson.getTitle());
        assertNotNull(buildingJson.getAddress());
        assertEquals("", buildingJson.getDescription());
        assertEquals(50F, buildingJson.getLivingSpace());
        assertEquals(0.0F, buildingJson.getCommercialSpace());
        assertEquals(25F, buildingJson.getUsableSpace());
        assertEquals(0.0F, buildingJson.getHeatingSpace());
    }
}
